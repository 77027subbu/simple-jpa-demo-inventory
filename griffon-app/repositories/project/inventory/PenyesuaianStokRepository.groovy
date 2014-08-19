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
package project.inventory

import domain.Container
import domain.event.PerubahanStok
import domain.exception.DataDuplikat
import domain.exception.DataTidakBolehDiubah
import domain.inventory.PenyesuaianStok
import domain.util.NomorService
import org.joda.time.LocalDate
import simplejpa.transaction.Transaction
import griffon.util.ApplicationHolder

@Transaction
class PenyesuaianStokRepository {

    List cari(LocalDate tanggalMulaiSearch, LocalDate tanggalSelesaiSearch, String nomorSearch, String gudangSearch) {
        findAllPenyesuaianStokByDsl([orderBy: 'tanggal,nomor', excludeDeleted: false]) {
            tanggal between(tanggalMulaiSearch, tanggalSelesaiSearch)
            if (nomorSearch) {
                and()
                nomor like("%${nomorSearch}%")
            }
            if (gudangSearch) {
                and()
                gudang__nama like("%${gudangSearch}%")
            }
        }
    }

    PenyesuaianStok buat(PenyesuaianStok penyesuaianStok) {
        penyesuaianStok.nomor = Container.app.nomorService.buatNomor(NomorService.TIPE.PENYESUAIAN_STOK)
        if (findPenyesuaianStokByNomor(penyesuaianStok.nomor)) {
            throw new DataDuplikat(penyesuaianStok)
        }
        penyesuaianStok.items.each { it.produk = merge(it.produk) }
        if (!penyesuaianStok.keterangan) penyesuaianStok.keterangan = 'Penyesuaian Stok'
        persist(penyesuaianStok)
        ApplicationHolder.application?.event(new PerubahanStok(penyesuaianStok, null))
        penyesuaianStok
    }

    PenyesuaianStok update(PenyesuaianStok penyesuaianStok) {
        PenyesuaianStok mergedPenyesuaianStok = findPenyesuaianStokById(penyesuaianStok.id)
        if (!mergedPenyesuaianStok) {
            throw new DataTidakBolehDiubah(mergedPenyesuaianStok)
        }
        mergedPenyesuaianStok.with {
            nomor = penyesuaianStok.nomor
            tanggal = penyesuaianStok.tanggal
            keterangan = penyesuaianStok.keterangan
        }
        mergedPenyesuaianStok
    }

    PenyesuaianStok hapus(PenyesuaianStok penyesuaianStok) {
        penyesuaianStok = findPenyesuaianStokById(penyesuaianStok.id)
        if (!penyesuaianStok) {
            throw new DataTidakBolehDiubah(penyesuaianStok)
        }
        penyesuaianStok.deleted = 'Y'
        ApplicationHolder.application?.event(new PerubahanStok(penyesuaianStok, null, true))
        penyesuaianStok
    }

}