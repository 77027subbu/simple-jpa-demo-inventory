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
package project.labarugi

import domain.labarugi.*
import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.joda.time.*
import util.SwingHelper
import org.jdesktop.swingx.combobox.EnumComboBoxModel

class TransaksiKasModel {

    @Bindable Long id

    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable String pihakTerkait
    BasicEventList<KategoriKas> kategoriKasList = new BasicEventList<>()
    DefaultEventComboBoxModel<KategoriKas> kategoriKas = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(kategoriKasList)
    @Bindable BigDecimal jumlah
    EnumComboBoxModel<JENIS_TRANSAKSI_KAS> jenisTransaksiKas = new EnumComboBoxModel<JENIS_TRANSAKSI_KAS>(JENIS_TRANSAKSI_KAS)
    @Bindable String keterangan
    BasicEventList<TransaksiKas> transaksiKasList = new BasicEventList<>()

    @Bindable String nomorSearch
    @Bindable String pihakTerkaitSearch
    @Bindable String kategoriKasSearch
    ListComboBoxModel jenisTransaksiSearch = new ListComboBoxModel(SwingHelper.searchEnum(JENIS_TRANSAKSI_KAS))
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

    @Bindable boolean deleted = false

}