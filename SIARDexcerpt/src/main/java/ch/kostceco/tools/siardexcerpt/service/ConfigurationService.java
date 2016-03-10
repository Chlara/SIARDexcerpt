/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt application is used for excerpt a record from a SIARD-File. Copyright (C) 2016
 * Claire R�thlisberger (KOST-CECO)
 * -----------------------------------------------------------------------------------------------
 * SIARDexcerpt is a development of the KOST-CECO. All rights rest with the KOST-CECO. This
 * application is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This application is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the follow GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA or see <http://www.gnu.org/licenses/>.
 * ============================================================================================== */

package ch.kostceco.tools.siardexcerpt.service;

/** Service Interface f�r die Konfigurationsdatei.
 * 
 * @author Rc Claire R�thlisberger, KOST-CECO */
public interface ConfigurationService extends Service
{
	// ------------------------ Allgemeines ------------------------
	/** Pfad zu XSL-File, damit der extrahierte Record dargestellt werden kann
	 * 
	 * @return Pfad des XSL-File zum extrahierten Record */
	String getPathToXSL();

	/** Pfad zu XSL-File, damit das Suchergebnis dargestellt werden kann
	 * 
	 * @return Pfad des XSL-File zum Suchergebnis */
	String getPathToXSLsearch();

	/** Gibt den Pfad des Arbeitsverzeichnisses zur�ck. Dieses Verzeichnis wird z.B. zum Entpacken des
	 * .zip-Files verwendet.
	 * 
	 * @return Pfad des Arbeitsverzeichnisses */
	String getPathToWorkDir();

	/** Gibt den Pfad des Outputverzeichnisses zur�ck.
	 * 
	 * @return Pfad des Outputverzeichnisses */
	String getPathToOutput();

	/** Gibt den Namen des Archivs zur�ck.
	 * 
	 * @return Namen des Archivs */
	String getArchive();

	// ------------------------ Suche ------------------------
	/** Gibt den Ordner der Suchtabelle zur�ck.
	 * 
	 * @return Ordner der Suchtabelle */
	String getSearchtableFolder();

	/** Gibt den Namen der Suchtabelle zur�ck.
	 * 
	 * @return Name der Suchtabelle */
	String getSearchtableName();

	/** Gibt den Namen der Suchzelle Nr1 zur�ck.
	 * 
	 * @return Name der Suchzelle Nr1 */
	String getcellName1();

	/** Gibt den Nummer der Suchzelle Nr1 zur�ck.
	 * 
	 * @return Nummer der Suchzelle Nr1 */
	String getcellNumber1();

	/** Gibt den Namen der Suchzelle Nr2 zur�ck.
	 * 
	 * @return Name der Suchzelle Nr2 */
	String getcellName2();

	/** Gibt den Nummer der Suchzelle Nr2 zur�ck.
	 * 
	 * @return Nummer der Suchzelle Nr2 */
	String getcellNumber2();

	/** Gibt den Namen der Suchzelle Nr3 zur�ck.
	 * 
	 * @return Name der Suchzelle Nr3 */
	String getcellName3();

	/** Gibt den Nummer der Suchzelle Nr3 zur�ck.
	 * 
	 * @return Nummer der Suchzelle Nr3 */
	String getcellNumber3();

	/** Gibt den Namen der Suchzelle Nr4 zur�ck.
	 * 
	 * @return Name der Suchzelle Nr4 */
	String getcellName4();

	/** Gibt den Nummer der Suchzelle Nr4 zur�ck.
	 * 
	 * @return Nummer der Suchzelle Nr4 */
	String getcellNumber4();

	/** Gibt den Namen der Suchzelle ResultNr1 zur�ck.
	 * 
	 * @return Name der Suchzelle ResultNr1 */
	String getcellNameResult1();

	/** Gibt den Nummer der Suchzelle ResultNr1 zur�ck.
	 * 
	 * @return Nummer der Suchzelle ResultNr1 */
	String getcellNumberResult1();

	/** Gibt den Namen der Suchzelle ResultNr2 zur�ck.
	 * 
	 * @return Name der Suchzelle ResultNr2 */
	String getcellNameResult2();

	/** Gibt den Nummer der Suchzelle ResultNr2 zur�ck.
	 * 
	 * @return Nummer der Suchzelle ResultNr2 */
	String getcellNumberResult2();

	/** Gibt den Namen der Suchzelle ResultNr3 zur�ck.
	 * 
	 * @return Name der Suchzelle ResultNr3 */
	String getcellNameResult3();

	/** Gibt den Nummer der Suchzelle NrResult3 zur�ck.
	 * 
	 * @return Nummer der Suchzelle NrResult3 */
	String getcellNumberResult3();

	/** Gibt den Namen der Suchzelle NrResult4 zur�ck.
	 * 
	 * @return Name der Suchzelle NrResult4 */
	String getcellNameResult4();

	/** Gibt den Nummer der Suchzelle NrResult4 zur�ck.
	 * 
	 * @return Nummer der Suchzelle NrResult4 */
	String getcellNumberResult4();

	// ------------------------ Extraktion ------------------------
	/** Gibt den Ordner der Haupttabelle zur�ck.
	 * 
	 * @return Ordner der Haupttabelle */
	String getMaintableFolder();

	/** Gibt den Namen der Haupttabelle zur�ck.
	 * 
	 * @return Name der Haupttabelle */
	String getMaintableName();

	/** Gibt den Namen des Prim�rschl�ssel der Haupttabelle zur�ck.
	 * 
	 * @return Namen des Prim�rschl�ssel der Haupttabelle */
	String getMaintablePrimarykeyName();

	/** Gibt die Zelle des Prim�rschl�ssel der Haupttabelle zur�ck.
	 * 
	 * @return Zelle des Prim�rschl�ssel der Haupttabelle */
	String getMaintablePrimarykeyCell();

}
