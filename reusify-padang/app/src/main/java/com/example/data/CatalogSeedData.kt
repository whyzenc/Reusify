package com.example.data

import com.example.data.model.Campus
import com.example.data.model.ItemStatus
import com.example.data.model.MarketItem

object CatalogSeedData {

  const val ADMIN_EMAIL = "whenyzenica@gmail.com"

  val categories = listOf(
    "Semua",
    "Elektronik",
    "Perabot Kos",
    "Dapur",
    "Buku Kuliah",
    "Alat Kuliah",
    "Transportasi",
    "Pakaian",
    "Lainnya",
  )

  val defaultCampuses = listOf(
    Campus(
      id = "UNP",
      code = "UNP",
      name = "Universitas Negeri Padang",
      city = "Padang",
      meetupSpot = "Gerbang Utama UNP Air Tawar / Auditorium UNP",
      active = true,
      createdBy = "system",
    ),
    Campus(
      id = "PNP",
      code = "PNP",
      name = "Politeknik Negeri Padang",
      city = "Padang",
      meetupSpot = "Gedung PKM Politeknik Negeri Padang, Limau Manis",
      active = true,
      createdBy = "system",
    ),
    Campus(
      id = "UNAND",
      code = "UNAND",
      name = "Universitas Andalas",
      city = "Padang",
      meetupSpot = "Auditorium UNAND / PKM Limau Manis",
      active = true,
      createdBy = "system",
    ),
    Campus(
      id = "UBH",
      code = "UBH",
      name = "Universitas Bung Hatta",
      city = "Padang",
      meetupSpot = "Kampus I Ulak Karang / Kampus II Aie Pacah",
      active = true,
      createdBy = "system",
    ),
  )

  private data class RawCatalogItem(
    val id: Int,
    val nama: String,
    val kategori: String,
    val harga: Long,
    val urlFoto: String,
    val deskripsi: String,
  )

  private data class SeedSeller(
    val id: String,
    val name: String,
    val campusCode: String,
    val campusName: String,
    val premium: Boolean,
  )

  private val seedSellers = listOf(
    SeedSeller("catalog_seller_unp", "Amelia Putri (UNP)", "UNP", "Universitas Negeri Padang", true),
    SeedSeller("catalog_seller_pnp", "Rizky Pratama (PNP)", "PNP", "Politeknik Negeri Padang", false),
    SeedSeller("catalog_seller_unand", "Nadia Salsabila (UNAND)", "UNAND", "Universitas Andalas", true),
    SeedSeller("catalog_seller_ubh", "Fajar Kurniawan (UBH)", "UBH", "Universitas Bung Hatta", false),
  )

  private val rawItems = listOf(
    RawCatalogItem(1, "Kipas Angin Meja Miyako", "Elektronik", 60000, "https://down-id.img.susercontent.com/file/id-11134207-81ztl-mf9hzavhv094f1", "Kipas angin meja 3 kecepatan, masih kencang dan tidak berisik. Dipakai 1 tahun di kos. Kabel utuh, baling-baling tidak retak."),
    RawCatalogItem(2, "Rice Cooker Mini Cosmos 0.6L", "Elektronik", 85000, "https://down-id.img.susercontent.com/file/id-11134207-7ra0n-mbdr2tp6grhg55", "Rice cooker kecil cocok untuk anak kos, kapasitas 0.6 liter. Berfungsi normal, panci anti lengket masih mulus. Dijual karena sudah lulus."),
    RawCatalogItem(3, "Dispenser Galon Atas Portable", "Elektronik", 110000, "https://5.imimg.com/data5/ANDROID/Default/2021/5/AK/DT/XQ/95812432/product-jpeg-500x500.jpg", "Dispenser galon atas fitur air panas dan normal. Pemakaian 8 bulan, tidak bocor. Cocok untuk kamar kos."),
    RawCatalogItem(4, "Lampu Belajar LED Meja", "Elektronik", 35000, "https://media.karousell.com/media/photos/products/2020/4/12/touch_table_lighting_miniso_le_1586694460_97212737_progressive.jpg", "Lampu belajar LED dengan leher fleksibel, cahaya putih nyaman di mata. Kabel panjang 1,5 meter."),
    RawCatalogItem(5, "Setrika Listrik Philips", "Elektronik", 50000, "local://setrika_philips", "Setrika listrik biasa, panas merata, sol masih bagus. Sering dipakai untuk baju kuliah dan kemeja praktikum."),
    RawCatalogItem(6, "Kompor Listrik Single Burner", "Elektronik", 95000, "https://down-id.img.susercontent.com/file/sg-11134201-7rauu-mat9rphvehk21d", "Kompor listrik 1 tungku 500W, aman untuk kos yang melarang kompor gas. Pemakaian normal, ada sedikit bekas gosong."),
    RawCatalogItem(7, "Kipas Angin Berdiri Maspion", "Elektronik", 130000, "https://media.karousell.com/media/photos/products/2021/6/19/strong16_mistral_electric_stan_1624103126_ec5aa97c.jpg", "Kipas berdiri tinggi bisa diatur, 3 kecepatan, putaran oscilating masih jalan. Minus ada goresan di tiang."),
    RawCatalogItem(8, "Teko Listrik Pemanas Air 1L", "Elektronik", 45000, "https://waziri.ng/wp-content/uploads/2023/12/Feng-Cheng-12cm-Electric-Heating-Cup-2.webp", "Teko listrik untuk rebus air, mi instan, dan seduh kopi. Auto mati, kondisi bersih."),
    RawCatalogItem(9, "Speaker Bluetooth JBL Go", "Elektronik", 150000, "https://media.karousell.com/media/photos/products/2021/10/23/jbl_go_2_bluetooth_speaker_1634989797_4bb9d927_progressive.jpg", "Speaker bluetooth kecil, suara masih jernih, baterai tahan sekitar 4 jam. Ada lecet halus di bodi."),
    RawCatalogItem(10, "Headset Gaming Rexus", "Elektronik", 120000, "https://tse2.mm.bing.net/th/id/OIP.OWhXksK9Z4kkqPmJ3DXijgHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3", "Headset gaming dengan mic, sudah dipakai 1 tahun. Suara normal, busa telinga sedikit aus."),
    RawCatalogItem(11, "Kasur Lipat Busa Single", "Perabot Kos", 150000, "https://sp-gallery.com/images/Article/NewsDetail-202507/831753/kasur-lipat-_-portable.jpg", "Kasur lipat busa ukuran single 90x190 cm, empuk dan bersih tanpa noda. Mudah dibawa pindah kos."),
    RawCatalogItem(12, "Bantal dan Guling Set", "Perabot Kos", 40000, "https://i.pinimg.com/1200x/84/a1/6d/84a16dcd0f89373f73298d8fb852058c.jpg", "Satu set bantal dan guling dakron, sudah dicuci dan disarungi baru. Siap pakai."),
    RawCatalogItem(13, "Rak Sepatu 4 Tingkat", "Perabot Kos", 45000, "https://i.pinimg.com/1200x/3b/c5/be/3bc5be39b6ab8c0e7c08c905a3ba07a5.jpg", "Rak sepatu plastik 4 tingkat, kokoh dan mudah dirakit. Muat sekitar 8 pasang sepatu."),
    RawCatalogItem(14, "Lemari Plastik Napolly 3 Pintu", "Perabot Kos", 250000, "https://i.pinimg.com/736x/25/31/95/2531954e2f2ab666c424fcb2c1bd76b0.jpg", "Lemari baju plastik 3 pintu, tidak ada engsel patah. Cocok untuk ruang kos kecil."),
    RawCatalogItem(15, "Meja Lipat Laptop Lesehan", "Perabot Kos", 30000, "https://i.pinimg.com/736x/2c/ff/ba/2cffba773e9cf9a570ff3069c3a583ea.jpg", "Meja lipat portable untuk belajar atau nonton di kasur. Kaki bisa dilipat, permukaan kayu mulus."),
    RawCatalogItem(16, "Rak Buku Kayu 3 Susun", "Perabot Kos", 90000, "https://i.pinimg.com/1200x/f1/6f/cf/f16fcfe0354c10cb438716da98bcad7e.jpg", "Rak buku kayu 3 susun, tinggi 100 cm. Kokoh untuk buku kuliah dan diktat."),
    RawCatalogItem(17, "Karpet Lantai 150x200", "Perabot Kos", 70000, "https://i.pinimg.com/736x/80/1d/22/801d22fca6b6a4b257872569a1958399.jpg", "Karpet bulu halus warna abu, sudah dicuci. Tidak ada robek, nyaman untuk lesehan."),
    RawCatalogItem(18, "Tirai Jendela Kamar Kos", "Perabot Kos", 25000, "https://i.etsystatic.com/6843952/r/il/ffde1d/1079483276/il_fullxfull.1079483276_qmfv.jpg", "Tirai kain motif polos dengan ring, ukuran 120x150 cm. Cocok untuk jendela kos."),
    RawCatalogItem(19, "Gantungan Baju Standing Hanger", "Perabot Kos", 55000, "https://media.karousell.com/media/photos/products/2023/9/26/clothes_rack_on_wheels_1695730539_4dc8f0f8_progressive.jpg", "Standing hanger besi dengan 8 kait, mudah dilepas pasang. Tidak berkarat."),
    RawCatalogItem(20, "Kursi Belajar Roda Kantor", "Perabot Kos", 175000, "https://p16-oec-sg.ibyteimg.com/tos-alisg-i-aphluv4xwc-sg/img/VqbcmM/2021/4/28/204268e1-1a97-49b1-b724-bfb770e53390.jpg~tplv-aphluv4xwc-resize-jpeg:800:800.jpeg?dr=15584&t=555f072d&ps=933b5bde&shp=6ce186a1&shcp=607f11de&idc=my&from=1826719393", "Kursi belajar dengan roda dan tinggi bisa diatur. Dudukan empuk, sedikit bunyi saat diputar."),
    RawCatalogItem(21, "Panci Set Stainless 3 Pcs", "Dapur", 65000, "https://down-id.img.susercontent.com/file/4157b0888db9b3fbba20d652c15576a8", "Satu set panci stainless 3 ukuran dengan tutup. Cocok untuk masak mie, sup, dan rebus telur."),
    RawCatalogItem(22, "Wajan Anti Lengket 24 cm", "Dapur", 40000, "https://www.chefstation.com.my/cms/images/2020/06/Frying-pan-sandwich-bottom.jpg?v=1592469081", "Wajan anti lengket ukuran 24 cm, lapisan masih bagus. Cocok untuk telur dan nasi goreng."),
    RawCatalogItem(23, "Set Piring Gelas Sendok", "Dapur", 50000, "https://ae01.alicdn.com/kf/S354d094f31e84722a238345f9f282b56s.jpg", "Satu set peralatan makan untuk 2 orang, melamin tahan banting. Termasuk sendok garpu."),
    RawCatalogItem(24, "Termos Air Panas 1L", "Dapur", 45000, "https://down-id.img.susercontent.com/file/14c9f7e0444b00be276094eea369ad65", "Termos stainless 1 liter, tahan panas hingga 8 jam. Tutup masih rapat."),
    RawCatalogItem(25, "Blender Mini Portable", "Dapur", 75000, "https://5.imimg.com/data5/SELLER/Default/2024/11/462839771/UY/XP/WU/119799934/img-20241104-wa0042-1000x1000.jpg", "Blender mini untuk jus dan smoothie, kapasitas 400 ml. Pisau tajam, pemakaian jarang."),
    RawCatalogItem(26, "Galon Kosong Aqua 19L", "Dapur", 35000, "https://down-id.img.susercontent.com/file/id-11134207-822wr-mq1fyhk6p4b0a0", "Galon kosong merek Aqua, kondisi bersih dan tidak bocor. Cocok untuk dispenser."),
    RawCatalogItem(27, "Toples Kaca Set 4 Pcs", "Dapur", 30000, "https://down-my.img.susercontent.com/file/sg-11134201-7rceh-m6jm3937d797b2", "Empat toples kaca dengan tutup untuk menyimpan snack, kopi, dan gula. Tidak ada yang retak."),
    RawCatalogItem(28, "Talenan dan Pisau Dapur", "Dapur", 25000, "https://i.pinimg.com/736x/66/fa/f0/66faf049903bb71132023bc1d5c34568.jpg", "Satu set talenan kayu dan pisau dapur stainless, masih tajam. Sering dipakai masak kos."),
    RawCatalogItem(29, "Buku Kalkulus Edisi 9 (Purcell)", "Buku Kuliah", 75000, "https://i.pinimg.com/1200x/fa/c0/73/fac0735bf590c811c6ebcf7681f4e45e.jpg", "Buku Kalkulus Purcell edisi 9 bahasa Indonesia, sedikit coretan pensil. Halaman lengkap tidak ada yang robek."),
    RawCatalogItem(30, "Buku Pemrograman Dasar Python", "Buku Kuliah", 55000, "https://i.pinimg.com/1200x/29/b6/22/29b622569e04c21506a1d1c157aa1856.jpg", "Buku pemrograman Python untuk pemula, kondisi seperti baru. Dipakai satu semester."),
    RawCatalogItem(31, "Buku Pengantar Akuntansi 1", "Buku Kuliah", 60000, "https://i.pinimg.com/1200x/2f/f7/55/2ff75535c78fc566915b5d6e226c6e14.jpg", "Buku akuntansi dasar dengan highlight kuning di beberapa bab. Cocok untuk mahasiswa semester awal."),
    RawCatalogItem(32, "Buku Fisika Dasar Halliday", "Buku Kuliah", 90000, "https://i.pinimg.com/736x/a3/5b/ef/a35befc5ed9a88edd16a950bf7f51446.jpg", "Fisika Dasar jilid 1, kondisi baik, sampul sedikit lusuh. Lengkap dengan soal latihan."),
    RawCatalogItem(33, "Diktat Statistika Dasar", "Buku Kuliah", 20000, "https://i.pinimg.com/1200x/ea/79/84/ea79844fb0d14f462981064f3ba72b53.jpg", "Diktat fotokopi jilid spiral, bersih tanpa coretan. Materi lengkap hingga bab 10."),
    RawCatalogItem(34, "Kamus Inggris-Indonesia Echols", "Buku Kuliah", 50000, "https://i.pinimg.com/1200x/f8/92/fe/f892fe214b0731206e95b4f3234c9da7.jpg", "Kamus bahasa Inggris tebal, halaman utuh. Sangat berguna untuk tugas dan skripsi."),
    RawCatalogItem(35, "Kalkulator Scientific Casio fx-991", "Alat Kuliah", 120000, "https://i.pinimg.com/1200x/81/9a/d8/819ad8b321e0a5cf25613bcee93101d9.jpg", "Kalkulator ilmiah Casio fx-991ES Plus, tombol normal dan layar jelas. Dilengkapi cover pelindung."),
    RawCatalogItem(36, "Jas Lab Putih Ukuran L", "Alat Kuliah", 45000, "https://i.pinimg.com/1200x/9d/91/1f/9d911fee6c4eae249a7c5ffe3b28c187.jpg", "Jas lab putih bersih, bahan katun, ukuran L. Dipakai satu semester praktikum."),
    RawCatalogItem(37, "Tas Ransel Laptop 15 Inci", "Alat Kuliah", 85000, "https://i.pinimg.com/736x/48/51/ed/4851ed347d95ca9848716bcb07b794cf.jpg", "Ransel dengan kompartemen laptop 15 inci, zipper lancar, tali tebal dan nyaman. Warna hitam."),
    RawCatalogItem(38, "Laptop Stand Lipat Aluminium", "Alat Kuliah", 60000, "https://i.pinimg.com/1200x/9a/c5/f4/9ac5f47112a45f658b5f4b7c618a4168.jpg", "Stand laptop lipat 6 tingkat ketinggian, kokoh dan ringan. Mengurangi pegal leher saat ngerjain tugas."),
    RawCatalogItem(39, "Mouse Wireless Logitech", "Alat Kuliah", 70000, "https://i.pinimg.com/1200x/a4/1c/03/a41c030881625b9708346b54133acd14.jpg", "Mouse wireless Logitech, termasuk receiver USB. Baterai baru diganti."),
    RawCatalogItem(40, "Flashdisk 32GB Sandisk", "Alat Kuliah", 40000, "https://i.pinimg.com/1200x/21/83/c9/2183c9ded2460a11a2bfcfc144b5be63.jpg", "Flashdisk 32GB masih sehat, bersih dari virus. Cocok untuk membawa tugas dan jurnal."),
    RawCatalogItem(41, "Whiteboard Kecil 40x60 cm", "Alat Kuliah", 50000, "https://i.pinimg.com/1200x/f6/04/72/f60472d00adf3b4220187402026a1d11.jpg", "Papan tulis kecil dengan frame aluminium. Cocok untuk belajar kelompok atau jadwal kos."),
    RawCatalogItem(42, "Sepeda Lipat Polygon Urbano", "Transportasi", 1200000, "https://i.pinimg.com/736x/f4/66/06/f466069a72032e17909cac27124744a3.jpg", "Sepeda lipat 16 inci, rem dan rantai normal. Praktis untuk keliling kampus, ada lecet cat."),
    RawCatalogItem(43, "Helm Half Face SNI", "Transportasi", 75000, "https://i.pinimg.com/1200x/c1/8b/d9/c18bd953790c9663eeca704d59f4e4fc.jpg", "Helm half face SNI warna hitam, busa dalam bersih. Kaca masih bening tanpa retak."),
    RawCatalogItem(44, "Jas Hujan Ponco", "Transportasi", 25000, "https://i.pinimg.com/1200x/59/a6/9f/59a69f9161339e12e27a09f5733f9151.jpg", "Jas hujan ponco ukuran XL, bahan tebal dan tidak bocor. Cocok untuk motoran ke kampus."),
    RawCatalogItem(45, "Jaket Hoodie Himpunan", "Pakaian", 65000, "https://i.pinimg.com/736x/e6/c1/43/e6c14340ab6cee299b7a81926390382d.jpg", "Hoodie bahan fleece tebal ukuran L, cuma dipakai beberapa kali. Cocok untuk kelas ber-AC."),
    RawCatalogItem(46, "Sepatu Sneakers Putih Size 42", "Pakaian", 110000, "https://i.pinimg.com/1200x/50/fe/13/50fe13dc5cf784bd6b99e4a15191086e.jpg", "Sneakers putih size 42, sol masih tebal. Ada noda kecil di sisi kiri."),
    RawCatalogItem(47, "Kemeja Putih Lengan Panjang M", "Pakaian", 35000, "https://i.pinimg.com/736x/83/4d/37/834d370d8a65f07464d3fcdb4141e3ee.jpg", "Kemeja putih untuk presentasi atau sidang, ukuran M. Bahan adem dan tidak kusut."),
    RawCatalogItem(48, "Celana Bahan Hitam Ukuran 32", "Pakaian", 45000, "https://i.pinimg.com/1200x/90/9d/68/909d68600d93ba3d8f8ec17a43ed2906.jpg", "Celana bahan hitam formal untuk kuliah dan seminar. Kondisi bagus tanpa sobek."),
    RawCatalogItem(49, "Sandal Gunung Eiger Size 41", "Pakaian", 60000, "https://i.pinimg.com/1200x/0e/35/ee/0e35eee93c4694e430843e7d3eaa95bf.jpg", "Sandal gunung Eiger sol karet tebal, awet untuk jalan kaki. Strap masih kuat."),
    RawCatalogItem(50, "Jam Dinding Analog", "Lainnya", 30000, "https://i.pinimg.com/1200x/42/8c/d6/428cd6b7f366f203f79ce89871cd7deb.jpg", "Jam dinding diameter 30 cm, mesin baterai AA jalan normal. Warna putih minimalis."),
    RawCatalogItem(51, "Ember dan Gayung Set", "Lainnya", 20000, "https://down-id.img.susercontent.com/file/id-11134207-7rasm-m5bv7y21a6s3a2", "Ember plastik 15 liter beserta gayung, bersih. Kebutuhan wajib anak kos."),
  )

  val catalogItems: List<MarketItem> = rawItems.mapIndexed { index, raw ->
    val seller = seedSellers[index % seedSellers.size]
    MarketItem(
      id = "catalog_${raw.id}",
      sellerId = seller.id,
      sellerName = seller.name,
      sellerCampusCode = seller.campusCode,
      sellerPremium = seller.premium,
      title = raw.nama,
      category = raw.kategori,
      campusCode = seller.campusCode,
      campusName = seller.campusName,
      price = raw.harga,
      description = raw.deskripsi,
      imageUrl = raw.urlFoto,
      status = ItemStatus.AVAILABLE,
      isFeatured = seller.premium && index < 6,
    )
  }
}
