/*
 * Copyright 2014 Jocki Hendry.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package domain.retur

import domain.exception.DataTidakBolehDiubah
import domain.exception.DataTidakKonsisten
import domain.faktur.Referensi
import domain.inventory.DaftarBarang
import domain.inventory.DaftarBarangSementara
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.SebuahDaftarBarang
import domain.penjualan.Konsumen
import domain.penjualan.PengeluaranBarang
import groovy.transform.*
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import project.user.NomorService
import simplejpa.DomainClass
import griffon.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.*
import org.joda.time.*

@DomainClass @Entity @Canonical(excludes='pengeluaranBarang')
class ReturJual implements SebuahDaftarBarang {

    @NotBlank @Size(min=2, max=100)
    String nomor

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @NotNull @ManyToOne
    Gudang gudang

    @Size(min=2, max=200)
    String keterangan

    @NotNull @ManyToOne
    Konsumen konsumen

    @NotNull
    Boolean sudahDiproses = false

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    PengeluaranBarang pengeluaranBarang

    @NotEmpty @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true) @OrderColumn @Valid
    List<ItemRetur> items = []

    void tambah(ItemRetur itemRetur) {
        // Periksa klaim untuk item retur ini
        if (itemRetur.jumlahBarangDitukar() > itemRetur.jumlah) {
            throw new DataTidakKonsisten("${itemRetur.produk.nama}: jumlah yang ditukar (${itemRetur.jumlahBarangDitukar()}) melebihi jumlah yang diterima (${itemRetur.jumlah})!")
        }
        items << itemRetur
    }

    Set<Klaim> getKlaims(Class clazz, boolean hanyaBelumDiproses = false) {
        Set<Klaim> hasil = [] as Set
        items.each { hasil.addAll(it.getKlaims(clazz, hanyaBelumDiproses)) }
        hasil
    }

    Set<KlaimTukar> getKlaimsTukar(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimTukar, hanyaBelumDiproses)
    }

    Set<KlaimPotongPiutang> getKlaimsPotongPiutang(boolean hanyaBelumDiproses = false) {
        getKlaims(KlaimPotongPiutang, hanyaBelumDiproses)
    }

    BigDecimal jumlahPotongPiutang() {
        getKlaimsPotongPiutang().sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    Integer jumlahDitukar() {
        items.sum { ItemRetur i -> i.jumlahBarangDitukar() }?: 0
    }

    BigDecimal sisaPotongPiutang() {
        getKlaimsPotongPiutang(true).sum { KlaimPotongPiutang k -> k.jumlah }?: 0
    }

    void prosesKlaimPotongPiutang() {
        getKlaimsPotongPiutang(true).each { it.proses() }
    }

    void proses(Klaim klaim) {
        klaim.proses()
        sudahDiproses = items.every { it.isSudahDiproses() }?: false
    }

    PengeluaranBarang tukar() {
        if (pengeluaranBarang) {
            throw new DataTidakBolehDiubah(this)
        }
        Set<KlaimTukar> klaimTukar = getKlaimsTukar(true)
        if (klaimTukar.empty) {
            throw new UnsupportedOperationException("Tidak ada penukaran yang dapat dilakukan untuk retur jual [$nomor]")
        }
        PengeluaranBarang pengeluaranBarang = new PengeluaranBarang(
            nomor: ApplicationHolder.application.serviceManager.findService('Nomor').buatNomor(NomorService.TIPE.PENGELUARAN_BARANG),
            tanggal: LocalDate.now(),
            gudang: gudang,
            keterangan: "Retur Jual [$nomor]"
        )
        DaftarBarangSementara hasilNormalisasi = new DaftarBarangSementara(klaimTukar)
        hasilNormalisasi.items.each { pengeluaranBarang.tambah(it) }
        klaimTukar.each { proses(it) }
        pengeluaranBarang.diterima(LocalDate.now(), konsumen.nama, '[Retur Jual]')
        this.pengeluaranBarang = pengeluaranBarang
        pengeluaranBarang
    }

    void potongPiutang() {
        if (konsumen==null) {
            throw new UnsupportedOperationException("Konsumen untuk [$this] harus di-isi sebelum melakukan pemotongan piutang!")
        }
        Referensi referensi = new Referensi(nomor, ReturJual)
        getKlaimsPotongPiutang(true).each { KlaimPotongPiutang k ->
            konsumen.potongPiutang(k.jumlah, referensi)
            proses(k)
        }
    }

    @Override
    DaftarBarang toDaftarBarang() {
        def itemBarangs = items.collect { new ItemBarang(it.produk, it.jumlah) }
        DaftarBarangSementara hasil = new DaftarBarangSementara(itemBarangs)
        hasil.nomor = nomor
        hasil.tanggal = tanggal
        hasil.keterangan = keterangan
        hasil
    }

    List<ItemRetur> normalisasi() {
        List<ItemRetur> hasil = []
        items.each { ItemRetur i ->
            ItemRetur itemReturSama = hasil.find { it.produk == i.produk }
            if (itemReturSama) {
                itemReturSama << i
            } else {
                hasil << new ItemRetur(i.produk, i.jumlah, new HashSet<Klaim>(i.klaims))
            }
        }
        hasil
    }

}

