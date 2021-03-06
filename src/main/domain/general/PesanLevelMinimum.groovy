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
package domain.general

import domain.inventory.Produk
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*

import org.joda.time.*

@DomainClass @Entity @Canonical(excludes='produk')
class PesanLevelMinimum extends Pesan {

    @NotNull @ManyToOne(fetch=FetchType.LAZY)
    Produk produk

    @SuppressWarnings("GroovyUnusedDeclaration")
    PesanLevelMinimum() {}

    PesanLevelMinimum(Produk produk, int jumlahAktual, int levelMinimum) {
        this.tanggal = LocalDateTime.now()
        this.produk = produk
        this.pesan = "Stok <span class='info'>${produk.nama}</span> ($jumlahAktual) mencapai level minimum <span class='info'>$levelMinimum</span>."
    }

    @Override
    boolean masihBerlaku() {
        !produk.periksaLevel()
    }

    @Override
    String jenisPesan() {
        "Level Minimum Stok"
    }

}

