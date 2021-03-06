/*
 * Copyright 2015 Jocki Hendry.
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
package project.penjualan

import simplejpa.swing.DialogUtils
import java.awt.event.KeyEvent
import java.text.NumberFormat
import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'showFakturBelumLunas', name: 'Lihat Faktur Belum Lunas...', closure: controller.showFakturBelumLunas)
}

panel(id: 'mainPanel') {
    borderLayout()

    panel(constraints: PAGE_START) {
        borderLayout()
        label('<html><b>Petunjuk:</b> <i>Cari dan pilih jenis pekerjaan di tabel dan tekan Enter untuk selesai!</i></html>',
                visible: bind{model.popupMode}, horizontalAlignment: CENTER, constraints: PAGE_START)
        panel(constraints: CENTER) {
            flowLayout(alignment: FlowLayout.LEADING)
            textField(id: 'namaSearch', columns: 20, text: bind('namaSearch', target: model, mutual: true),
                actionPerformed: controller.search, keyPressed: { KeyEvent k ->
                    if (k.keyCode==KeyEvent.VK_DOWN) table.requestFocusInWindow()
                }
            )
            textField(id: 'salesSearch', columns: 20, text: bind('salesSearch', target: model, mutual: true),
                actionPerformed: controller.search, keyPressed: { KeyEvent k ->
                    if (k.keyCode==KeyEvent.VK_DOWN) table.requestFocusInWindow()
                }
            )
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }
    }

    scrollPane(constraints: CENTER) {
        glazedTable(id: 'table', list: model.konsumenList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                doubleClickAction: showFakturBelumLunas, enterKeyAction: showFakturBelumLunas) {
            glazedColumn(name: 'Nama', property: 'nama', preferredWidth: 300)
            glazedColumn(name: 'Nomor Telepon', property: 'nomorTelepon', preferredWidth: 100)
            glazedColumn(name: 'Region', property: 'region')
            glazedColumn(name: 'Sales', expression: { it.sales.nama })
            glazedColumn(name: 'Total Poin', property: 'poinTerkumpul', columnClass: Integer)
            glazedColumn(name: 'Credit Limit', property: 'creditLimit', columnClass: Integer, visible: bind {!model.popupMode}) {
                templateRenderer(exp: { it==null ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Credit Terpakai', property: 'creditTerpakai', columnClass: Integer, visible: bind {!model.popupMode}) {
                templateRenderer(exp: { it==null ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
            }
            glazedColumn(name: 'Penggunaan Credit (%)', expression: { it.getRatioPenggunaanCredit() }, columnClass: Integer, visible: bind {!model.popupMode}) {
                templateRenderer(exp: { NumberFormat.percentInstance.format(it) }, horizontalAlignment: RIGHT) {
                    condition(if_: {it>=1}, then_property_: 'foreground', is_: Color.RED, else_is_: Color.BLACK)
                    condition(if_: {isSelected}, then_property_: 'foreground', is_: Color.WHITE)
                }
            }
        }
    }

    panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: PAGE_END, focusCycleRoot: true) {
        label('Nama:')
        textField(id: 'nama', columns: 20, text: bind('nama', target: model, mutual: true), errorPath: 'nama')
        errorLabel(path: 'nama', constraints: 'wrap')
        label('Nomor Telepon:')
        textField(id: 'nomorTelepon', columns: 20, text: bind('nomorTelepon', target: model, mutual: true), errorPath: 'nomorTelepon')
        errorLabel(path: 'nomorTelepon', constraints: 'wrap')
        label('Alamat:')
        textField(id: 'alamat', columns: 50, text: bind('alamat', target: model, mutual: true), errorPath: 'alamat')
        errorLabel(path: 'alamat', constraints: 'wrap')
        label('Region:')
        comboBox(id: 'region', model: model.region, errorPath: 'region')
        errorLabel(path: 'region', constraints: 'wrap')
        label('Sales:')
        comboBox(id: 'sales', model: model.sales, templateRenderer: 'nama', errorPath: 'sales')
        errorLabel(path: 'sales', constraints: 'wrap')

        panel(constraints: 'span, growx, wrap') {
            flowLayout(alignment: FlowLayout.LEADING)
            button(app.getMessage("simplejpa.dialog.save.button"), actionPerformed: {
                if (model.id != null) {
                    if (!DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.update.message"), app.getMessage("simplejpa.dialog.update.title"), JOptionPane.WARNING_MESSAGE)) {
                        return
                    }
                }
                controller.save()
                form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
            })
            button('Credit Limit', visible: bind { table.isRowSelected }, actionPerformed: controller.creditLimit)
            button(action: showFakturBelumLunas, visible: bind { table.isRowSelected })
            button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {
                table.isRowSelected
            }, actionPerformed: controller.clear)
            button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind {
                table.isRowSelected
            }, actionPerformed: {
                if (DialogUtils.confirm(mainPanel, app.getMessage("simplejpa.dialog.delete.message"), app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.WARNING_MESSAGE)) {
                    controller.delete()
                }
            })
        }
    }
}

PromptSupport.setPrompt("Nama...", namaSearch)
PromptSupport.setPrompt("Sales...", salesSearch)