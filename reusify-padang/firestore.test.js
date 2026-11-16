const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const ts = () => new Date(Date.now() - 2000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_unp_123";
const BOB_UID = "bob_unand_456";
const ADMIN_UID = "admin_padang_001";
const ADMIN_EMAIL = "whenyzenica@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read items, campuses, users, or transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("items").where("status", "==", "available").get());
  await assertFails(unauthDb.collection("campuses").where("active", "==", true).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("transactions").where("buyerId", "==", ALICE_UID).get());
});

test("User profile PII isolation and self-promotion prevention", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@student.unp.ac.id",
    email_verified: true,
  }).firestore();

  // Cannot create profile with isPremium: true
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice UNP",
      email: "alice@student.unp.ac.id",
      campusCode: "UNP",
      campusName: "Universitas Negeri Padang",
      isPremium: true,
      isVerifiedStudent: true,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Valid profile creation with isPremium: false
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice UNP",
      email: "alice@student.unp.ac.id",
      campusCode: "UNP",
      campusName: "Universitas Negeri Padang",
      isPremium: false,
      isVerifiedStudent: true,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Bob cannot read Alice's PII profile
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@student.unand.ac.id",
    email_verified: true,
  }).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Alice cannot update her own isPremium to true
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isPremium: true,
      updatedAt: ts(),
    })
  );

  // Alice cannot inject ghost fields (Shadow Update Test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: ts(),
    })
  );
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofedAdminDb.collection("campuses").doc("UINIB").set({
      code: "UINIB",
      name: "UIN Imam Bonjol",
      city: "Padang",
      meetupSpot: "Gerbang Kampus Sungai Bangek",
      active: true,
      createdBy: "spoof_uid",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );
});

test("Admin can add campuses and approve premium status; student cannot", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@unp.ac.id",
    email_verified: true,
  }).firestore();

  // Student cannot create campus
  await assertFails(
    aliceDb.collection("campuses").doc("UPI").set({
      code: "UPI",
      name: "UPI YPTK Padang",
      city: "Padang",
      meetupSpot: "Gerbang UPI Lubuk Begalung",
      active: true,
      createdBy: ALICE_UID,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Verified admin can create campus
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("campuses").doc("UPI").set({
      code: "UPI",
      name: "UPI YPTK Padang",
      city: "Padang",
      meetupSpot: "Gerbang UPI Lubuk Begalung",
      active: true,
      createdBy: ADMIN_UID,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Student can list active campuses with matching query
  await assertSucceeds(
    aliceDb.collection("campuses").where("active", "==", true).get()
  );
  // Unfiltered campuses list fails for student
  await assertFails(aliceDb.collection("campuses").get());
});

test("Marketplace items and QR/COD transactions lifecycle with terminal locking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Alice lists an item
  await assertSucceeds(
    aliceDb.collection("items").doc("item_1").set({
      sellerId: ALICE_UID,
      sellerName: "Alice UNP",
      sellerCampusCode: "UNP",
      sellerPremium: false,
      title: "Kalkulus Purcell Edisi 9",
      category: "Buku Kuliah",
      campusCode: "UNP",
      campusName: "Universitas Negeri Padang",
      price: 75000,
      description: "Kondisi 90% mulus, siap COD di gerbang UNP",
      imageUrl: "https://example.com/book.jpg",
      status: "available",
      isFeatured: false,
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Query alignment: available items succeeds, unfiltered items fails
  await assertSucceeds(aliceDb.collection("items").where("status", "==", "available").get());
  await assertFails(aliceDb.collection("items").get());

  // Bob buys Alice's item via QR Code
  await assertSucceeds(
    bobDb.collection("items").doc("item_1").update({
      status: "sold",
      buyerId: BOB_UID,
      updatedAt: ts(),
    })
  );

  // Create transaction record
  await assertSucceeds(
    bobDb.collection("transactions").doc("tx_1").set({
      transactionCode: "RSF-20261116-0001",
      itemId: "item_1",
      itemTitle: "Kalkulus Purcell Edisi 9",
      itemImageUrl: "https://example.com/book.jpg",
      campusCode: "UNP",
      amount: 75000,
      paymentMethod: "QR_CODE",
      qrPayload: "REUSIFY-PADANG|TX:tx_1|AMT:75000",
      status: "paid",
      buyerId: BOB_UID,
      buyerName: "Bob UNAND",
      buyerCampusCode: "UNAND",
      sellerId: ALICE_UID,
      sellerName: "Alice UNP",
      createdAt: ts(),
      updatedAt: ts(),
    })
  );

  // Both buyer and seller can query their respective histories
  await assertSucceeds(bobDb.collection("transactions").where("buyerId", "==", BOB_UID).get());
  await assertSucceeds(aliceDb.collection("transactions").where("sellerId", "==", ALICE_UID).get());

  // Seller marks transaction completed
  await assertSucceeds(
    aliceDb.collection("transactions").doc("tx_1").update({
      status: "completed",
      updatedAt: ts(),
    })
  );

  // Terminal state lock: once completed, buyer/seller cannot revert status
  await assertFails(
    bobDb.collection("transactions").doc("tx_1").update({
      status: "cancelled",
      updatedAt: ts(),
    })
  );
});
