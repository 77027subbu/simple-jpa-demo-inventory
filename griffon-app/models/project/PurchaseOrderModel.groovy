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
package project

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel
import ca.odell.glazedlists.swing.GlazedListsSwing
import domain.Container
import domain.faktur.ItemFaktur
import domain.pembelian.PurchaseOrder
import domain.pembelian.StatusPurchaseOrder
import domain.pembelian.Supplier
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.joda.time.LocalDate

class PurchaseOrderModel {

    MainGroupModel.POViewMode mode
    @Bindable boolean showPenerimaan
    @Bindable boolean showFakturBeli
    @Bindable boolean allowTambahProduk
    @Bindable boolean allowAddPO

    @Bindable Long id
    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable BigDecimal diskonPotonganPersen
    @Bindable BigDecimal diskonPotonganLangsung
    @Bindable String keterangan
    List<ItemFaktur> listItemFaktur = []
    BasicEventList<Supplier> supplierList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Supplier> supplier = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(supplierList)
    ListComboBoxModel statusSearch = new ListComboBoxModel(Container.app.searchEnum(StatusPurchaseOrder))

    BasicEventList<PurchaseOrder> purchaseOrderList = new BasicEventList<>()

    @Bindable String nomorSearch
    @Bindable String supplierSearch
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch

    @Bindable String informasi

}