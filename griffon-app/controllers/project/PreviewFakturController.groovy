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

import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.swing.JRViewer

import javax.imageio.ImageIO
import java.awt.BorderLayout

class PreviewFakturController {
    // these will be injected by Griffon
    def model
    def view

    void mvcGroupInit(Map args) {
        List dataSource = [args.'dataSource']
        String fileLaporan = "report/${args.'fileLaporan'}.jasper"

        Map parameters = [:]
        InputStream logoInputStream = getResourceAsStream('report/logo.jpg')
        if (logoInputStream) parameters.'logo' = ImageIO.read(logoInputStream)

        JRBeanCollectionDataSource jrDataSource = new JRBeanCollectionDataSource(dataSource)
        JasperPrint jasperPrint = JasperFillManager.fillReport(getResourceAsStream(fileLaporan), parameters, jrDataSource)

        view.mainPanel.clear()
        view.mainPanel.add(new JRViewer(jasperPrint), BorderLayout.CENTER)
    }

}
