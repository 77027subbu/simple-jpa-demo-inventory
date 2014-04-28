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

import ca.odell.glazedlists.*
import domain.inventory.Gudang

class GudangModel {

    @Bindable Long id
	@Bindable String nama
	@Bindable Boolean utama
	@Bindable String keterangan

    @Bindable String namaSearch
    @Bindable String searchMessage

    BasicEventList<Gudang> gudangList = new BasicEventList<>()

}