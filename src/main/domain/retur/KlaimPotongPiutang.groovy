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

import domain.inventory.Produk
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import java.text.NumberFormat

@DomainClass @Entity @Canonical @AutoClone
class KlaimPotongPiutang extends Klaim {

    @Min(0l) @NotNull
    BigDecimal jumlah

    @Override
    void merge(Klaim klaim) {
        if (bolehMerge(klaim)) {
            jumlah += klaim.jumlah
        }
    }

    @Override
    boolean bolehMerge(Klaim klaim) {
        klaim instanceof KlaimPotongPiutang
    }

    @Override
    BigDecimal informasiHarga() {
        jumlah
    }

    @Override
    Produk informasiProduk() {
        null
    }

    @Override
    Integer informasiQty() {
        null
    }

    @Override
    boolean equals(Object o) {
        if (id == null || o.id == null) {
            return jumlah == o.jumlah
        } else if ((o instanceof KlaimPotongPiutang) && (id == o.id)) {
            return true
        }
        false
    }

    @Override
    String toString() {
        "Potong Piutang: ${jumlah? NumberFormat.currencyInstance.format(jumlah): 0}"
    }

}

