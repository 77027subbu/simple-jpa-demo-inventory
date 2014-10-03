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
package domain.penjualan

import domain.exception.PencairanPoinTidakValid
import domain.faktur.Pembayaran
import domain.faktur.Referensi
import domain.inventory.DaftarBarang
import domain.inventory.Produk
import groovy.transform.*
import org.joda.time.LocalDate
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*
import java.text.NumberFormat

@NamedEntityGraphs([
    @NamedEntityGraph(name='Konsumen.Complete', attributeNodes = [
        @NamedAttributeNode(value='listFakturBelumLunas', subgraph='faktur'),
        @NamedAttributeNode('hargaTerakhir')
    ], subgraphs = [
        @NamedSubgraph(name='faktur', attributeNodes = [
            @NamedAttributeNode('listItemFaktur'),
            @NamedAttributeNode('piutang')
        ])
    ]),
    @NamedEntityGraph(name='Konsumen.FakturBelumLunas', attributeNodes = [
        @NamedAttributeNode(value='listFakturBelumLunas', subgraph='faktur'),
    ], subgraphs = [
        @NamedSubgraph(name='faktur', attributeNodes = [
            @NamedAttributeNode('listItemFaktur'),
            @NamedAttributeNode('piutang'),
        ])
    ])
])
@DomainClass @Entity @Canonical(excludes='listFakturBelumLunas,listRiwayatPoin,hargaTerakhir')
class Konsumen implements Comparable {

    @NotEmpty @Size(min=2, max=100)
    String nama

    @Size(min=2, max=50)
    String nomorTelepon

    @Size(min=3, max=200)
    String alamat

    @NotNull @ManyToOne
    Region region

    @NotNull @ManyToOne
    Sales sales

    @NotNull
    BigDecimal creditLimit = BigDecimal.ZERO

    @NotNull
    BigDecimal creditTerpakai = BigDecimal.ZERO

    @NotNull
    Integer poinTerkumpul = 0

    @ElementCollection @OrderColumn
    List<RiwayatPoin> listRiwayatPoin = []

    @OneToMany @JoinTable
    Set<FakturJualOlehSales> listFakturBelumLunas = [] as Set

    @ElementCollection
    Map<Produk, BigDecimal> hargaTerakhir = [:]

    public BigDecimal jumlahPiutang() {
        listFakturBelumLunas.sum {
            (it.deleted == 'Y')? 0: (it.piutang ? it.sisaPiutang() : it.total())
        }?: 0
    }

    public boolean adaTagihanJatuhTempo() {
        listFakturBelumLunas.any { it.sudahJatuhTempo() }
    }

    public boolean bolehKredit(BigDecimal pengajuan) {
        if (adaTagihanJatuhTempo()) return false
        if ((jumlahPiutang() + pengajuan) > creditLimit) return false
        true
    }

    public void tambahFakturBelumLunas(FakturJualOlehSales faktur) {
        if (!listFakturBelumLunas.contains(faktur)) {
            listFakturBelumLunas << faktur
            creditTerpakai += faktur.total()
        }
    }

    public void hapusFakturBelumLunas(FakturJualOlehSales faktur) {
        listFakturBelumLunas.remove(faktur)
        creditTerpakai -= faktur.total()
    }

    public BigDecimal getRatioPenggunaanCredit() {
        if (creditLimit==0) {
            return 0
        } else {
            return creditTerpakai / creditLimit
        }
    }

    public BigDecimal hargaTerakhir(Produk produk) {
        if (hargaTerakhir.containsKey(produk)) {
            return hargaTerakhir[produk]
        } else {
            return produk.hargaUntuk(sales)
        }
    }

    public void potongPiutang(BigDecimal jumlah, Referensi referensi = null) {
        BigDecimal jumlahPiutangYangDapatDibayar = listFakturBelumLunas.sum {it.piutang? it.sisaPiutang(): 0}?: 0
        if (jumlah > jumlahPiutangYangDapatDibayar) {
            throw new IllegalStateException("Jumlah piutang yang akan dipotong melebihi jumlah piutang yang dapat dibayar: ${NumberFormat.currencyInstance.format(jumlahPiutangYangDapatDibayar)}!")
        }

        // Mengubah daftar faktur menjadi berurut berdasarkan tanggal
        List daftarFaktur = (listFakturBelumLunas as List).sort { f1, f2 -> (!f1?.tanggal || !f2?.tanggal)? -1: f1.tanggal.compareTo(f2.tanggal)}

        for (FakturJualOlehSales faktur: daftarFaktur) {
            if (!faktur.piutang) continue
            BigDecimal sisaPiutang = faktur.sisaPiutang()
            if (jumlah >= sisaPiutang) {
                // Lunasi seluruh piutang untuk faktur ini
                faktur.bayar(new Pembayaran(tanggal: LocalDate.now(), jumlah: sisaPiutang, potongan: true, referensi: referensi))
                jumlah -= sisaPiutang
            } else {
                // Lunasi hanya sebesar jumlah
                faktur.bayar(new Pembayaran(tanggal: LocalDate.now(), jumlah: jumlah, potongan: true, referensi: referensi))
                jumlah = 0
            }

            if (jumlah==0) break
        }
    }

    public void tambahPoin(Integer poin, String referensi = null) {
        if (poin == 0) return
        RiwayatPoin riwayatPoin = new RiwayatPoin(tanggal: LocalDate.now(), poin: poin, referensi: referensi)
        listRiwayatPoin << riwayatPoin
        this.poinTerkumpul += poin
    }

    public void tambahPoin(DaftarBarang daftarBarang) {
        tambahPoin(daftarBarang.toPoin(), daftarBarang.nomor)
    }

    public void hapusPoin(Integer poin, String referensi = null) {
        if (poin == 0) return
        RiwayatPoin riwayatPoin = new RiwayatPoin(tanggal: LocalDate.now(), poin: -poin, referensi: referensi)
        listRiwayatPoin << riwayatPoin
        this.poinTerkumpul -= poin
        if (this.poinTerkumpul < 0) this.poinTerkumpul = 0
    }

    public void hapusPoin(DaftarBarang daftarBarang) {
        hapusPoin(daftarBarang.toPoin(), daftarBarang.nomor)
    }

    @Override
    String toString() {
        "${nama} - Sales: ${sales?.nama} - Region: ${region?.nama}"
    }

    @Override
    int compareTo(Object o) {
        if (o == null) return -1
        if (!(o instanceof Konsumen)) return -1
        nama?.compareTo(o.nama)?: -1
    }
}

