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

import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.JENIS_TRANSAKSI_KAS
import domain.labarugi.SaldoKas
import domain.labarugi.KategoriKas
import griffon.test.*

class KategoriKasTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testJumlah() {
        KategoriKas k = new KategoriKas('Test', JENIS_KATEGORI_KAS.PENDAPATAN, false)

        k.listJumlahKas << new SaldoKas(1, 2014, 10000, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.listJumlahKas << new SaldoKas(1, 2014, 15000, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        k.listJumlahKas << new SaldoKas(2, 2014, 20000, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.listJumlahKas << new SaldoKas(2, 2014, 25000, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        k.listJumlahKas << new SaldoKas(3, 2014, 30000, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.listJumlahKas << new SaldoKas(3, 2014, 35000, JENIS_TRANSAKSI_KAS.LUAR_KOTA)

        // Jumlah untuk bulan Januari
        assertEquals(25000, k.saldo(1, 2014))
        assertEquals(10000, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(15000, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))

        // Jumlah untuk bulan Februari
        assertEquals(45000, k.saldo(2, 2014))
        assertEquals(20000, k.saldo(2, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(25000, k.saldo(2, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))

        // Jumlah untuk bulan Maret
        assertEquals(65000, k.saldo(3, 2014))
        assertEquals(30000, k.saldo(3, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(35000, k.saldo(3, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))
    }

    void testPerubahanSaldo() {
        KategoriKas k = new KategoriKas('Test', JENIS_KATEGORI_KAS.PENDAPATAN, false)

        k.perubahanSaldo(1, 2014, 10000.0, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.perubahanSaldo(1, 2014, 15000.0, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        assertEquals(10000.0, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(15000.0, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))
        assertEquals(2, k.listJumlahKas.size())

        k.perubahanSaldo(1, 2014, 5000.0, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.perubahanSaldo(1, 2014, 6000.0, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        assertEquals(15000.0, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(21000.0, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))
        assertEquals(2, k.listJumlahKas.size())

        k.perubahanSaldo(1, 2014, -1000.0, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.perubahanSaldo(1, 2014, -2000.0, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        assertEquals(14000.0, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(19000.0, k.saldo(1, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))
        assertEquals(2, k.listJumlahKas.size())

        k.perubahanSaldo(2, 2014, 1000.0, JENIS_TRANSAKSI_KAS.DALAM_KOTA)
        k.perubahanSaldo(2, 2014, 2000.0, JENIS_TRANSAKSI_KAS.LUAR_KOTA)
        assertEquals(1000.0, k.saldo(2, 2014, JENIS_TRANSAKSI_KAS.DALAM_KOTA))
        assertEquals(2000.0, k.saldo(2, 2014, JENIS_TRANSAKSI_KAS.LUAR_KOTA))
        assertEquals(4, k.listJumlahKas.size())
    }

}
