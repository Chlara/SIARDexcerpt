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

package ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.impl;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.siardexcerpt.exception.moduleexcerpt.ExcerptCGrepException;
import ch.kostceco.tools.siardexcerpt.excerption.ValidationModuleImpl;
import ch.kostceco.tools.siardexcerpt.excerption.moduleexcerpt.ExcerptCGrepModule;
import ch.kostceco.tools.siardexcerpt.service.ConfigurationService;
import ch.kostceco.tools.siardexcerpt.util.StreamGobbler;
import ch.kostceco.tools.siardexcerpt.util.Util;

/** Besteht eine korrekte prim�re Verzeichnisstruktur: /header/metadata.xml sowie
 * /header/metadata.xsd und /content */
public class ExcerptCGrepModuleImpl extends ValidationModuleImpl implements ExcerptCGrepModule
{

	private ConfigurationService	configurationService;

	public static String					NEWLINE	= System.getProperty( "line.separator" );

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public boolean validate( File siardDatei, File outFile, String excerptString )
			throws ExcerptCGrepException
	{
		// Ausgabe -> Ersichtlich das SIARDexcerpt arbeitet
		int onWork = 41;

		boolean isValid = true;

		File fGrepExe = new File( "resources" + File.separator + "grep" + File.separator + "grep.exe" );
		String pathToGrepExe = fGrepExe.getAbsolutePath();
		if ( !fGrepExe.exists() ) {
			// grep.exe existiert nicht --> Abbruch
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C )
							+ getTextResourceService().getText( ERROR_XML_C_MISSINGFILE,
									fGrepExe.getAbsolutePath() ) );
			return false;
		} else {
			File fMsys10dll = new File( "resources" + File.separator + "grep" + File.separator
					+ "msys-1.0.dll" );
			if ( !fMsys10dll.exists() ) {
				// msys-1.0.dll existiert nicht --> Abbruch
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_C )
								+ getTextResourceService().getText( ERROR_XML_C_MISSINGFILE,
										fMsys10dll.getAbsolutePath() ) );
				return false;
			}
		}

		File tempOutFile = new File( outFile.getAbsolutePath() + ".tmp" );
		String content = "";

		// Record aus Maintable herausholen
		try {
			if ( tempOutFile.exists() ) {
				Util.deleteDir( tempOutFile );
			}

			/* Nicht vergessen in "src/main/resources/config/applicationContext-services.xml" beim
			 * entsprechenden Modul die property anzugeben: <property name="configurationService"
			 * ref="configurationService" /> */

			// String name = getConfigurationService().getMaintableName();
			String folder = getConfigurationService().getMaintableFolder();
			String cell = getConfigurationService().getMaintablePrimarykeyCell();
			String tabfolder = "";
			String tabname = "";
			String tabdescription = "";
			String tabdescriptionProv = "";
			String cellname = "";
			String celldescription = "";

			File fMaintable = new File( siardDatei.getAbsolutePath() + File.separator + "content"
					+ File.separator + "schema0" + File.separator + folder + File.separator + folder + ".xml" );

			try {
				// grep "<c11>7561234567890</c11>" table13.xml >> output.txt
				String command = "cmd /c \"" + pathToGrepExe + " \"<" + cell + ">" + excerptString + "</"
						+ cell + ">\" " + fMaintable.getAbsolutePath() + " >> " + tempOutFile.getAbsolutePath()
						+ "\"";
				/* Das redirect Zeichen verunm�glicht eine direkte eingabe. mit dem geschachtellten Befehl
				 * gehts: cmd /c\"urspruenlicher Befehl\" */

				// System.out.println( command );

				Process proc = null;
				Runtime rt = null;

				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_ELEMENT_OPEN, folder ) );

				// Informationen zur Tabelle aus metadata.xml herausholen

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				// dbf.setValidating(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				org.w3c.dom.Document doc = db.parse( new FileInputStream( new File( siardDatei
						.getAbsolutePath() + File.separator + "header" + File.separator + "metadata.xml" ) ) );
				doc.getDocumentElement().normalize();

				dbf.setFeature( "http://xml.org/sax/features/namespaces", false );

				NodeList nlTable = doc.getElementsByTagName( "table" );
				for ( int i = 0; i < nlTable.getLength(); i++ ) {
					Node nodenlTable = nlTable.item( i );
					NodeList childNodes = nodenlTable.getChildNodes();
					for ( int x = 0; x < childNodes.getLength(); x++ ) {
						Node subNodeI = childNodes.item( x );
						if ( subNodeI.getNodeName().equals( "folder" ) ) {
							// System.out.println( subNodeI.getTextContent() );
							if ( subNodeI.getTextContent().equals( folder ) ) {
								/* Es ist die richtige Tabelle. Ensprechend wird i ans ende Gestellt, damit die
								 * i-Schlaufe beendet wird. */
								tabfolder = subNodeI.getTextContent();
								i = nlTable.getLength();
							}
						} else if ( subNodeI.getNodeName().equals( "name" ) ) {
							tabname = subNodeI.getTextContent();
						} else if ( subNodeI.getNodeName().equals( "description" ) ) {
							tabdescriptionProv = new String( subNodeI.getTextContent().getBytes(), "UTF-8" );
							/* in der description generiert mit csv2siard wird nach "word" der Select Befehl
							 * angeh�ngt. Dieser soll nicht mit ausgegeben werden. */
							String word = "\\u000A";
							int endIndex = tabdescriptionProv.indexOf( word );
							tabdescription = tabdescriptionProv.substring( 0, endIndex );
						} else if ( subNodeI.getNodeName().equals( "columns" ) ) {
							NodeList childNodesColumns = subNodeI.getChildNodes();
							for ( int y = 0; y < childNodesColumns.getLength(); y++ ) {
								Node subNodeII = childNodesColumns.item( y );
								NodeList childNodesColumn = subNodeII.getChildNodes();
								for ( int z = 0; z < childNodesColumn.getLength(); z++ ) {
									int cellNumber = (y + 1) / 2;
									// System.out.println( "Zelle Nr " + cellNumber );
									Node subNodeIII = childNodesColumn.item( z );
									if ( subNodeIII.getNodeName().equals( "name" ) ) {
										cellname = cellname + "<c" + cellNumber + ">" + subNodeIII.getTextContent()
												+ "</c" + cellNumber + ">";
									} else if ( subNodeIII.getNodeName().equals( "description" ) ) {
										celldescription = celldescription + "<c" + cellNumber + ">"
												+ new String( subNodeIII.getTextContent().getBytes(), "UTF-8" ) + "</c"
												+ cellNumber + ">";
									}
								}
							}
						}
					}
				}

				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_TEXT, tabname, "tabname" ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_TEXT, tabfolder, "tabfolder" ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_TEXT, tabdescription, "tabdescription" ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_TEXT, cellname, "name" ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_TEXT, celldescription, "description" ) );

				try {
					Util.switchOffConsole();
					rt = Runtime.getRuntime();
					proc = rt.exec( command.toString().split( " " ) );
					// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

					// Fehleroutput holen
					StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

					// Output holen
					StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

					// Threads starten
					errorGobbler.start();
					outputGobbler.start();

					// Warte, bis wget fertig ist
					proc.waitFor();

					Util.switchOnConsole();

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				} finally {
					if ( proc != null ) {
						closeQuietly( proc.getOutputStream() );
						closeQuietly( proc.getInputStream() );
						closeQuietly( proc.getErrorStream() );
					}
				}

				Scanner scanner = new Scanner( tempOutFile );
				content = "";
				try {
					content = scanner.useDelimiter( "\\Z" ).next();
				} catch ( Exception e ) {
					// Grep ergab kein treffer Content Null
					content = "";
				}
				scanner.close();

				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_ELEMENT_CONTENT, content ) );
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_ELEMENT_CLOSE, folder ) );

				if ( tempOutFile.exists() ) {
					Util.deleteDir( tempOutFile );
				}
				content = "";

				// Ende Grep

			} catch ( Exception e ) {
				getMessageService().logError(
						getTextResourceService().getText( MESSAGE_XML_MODUL_C )
								+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				return false;
			}

		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		// Ende MainTable

		// grep der SubTables
		try {
			// String name = null;
			String folder = null;
			String cell = null;

			InputStream fin = new FileInputStream( new File( "configuration" + File.separator
					+ "SIARDexcerpt.conf.xml" ) );
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build( fin );
			fin.close();

			/* read the document and for each subTable */
			Namespace ns = Namespace.getNamespace( "" );

			// select schema elements and loop
			List<Element> subtables = document.getRootElement().getChild( "subtables", ns )
					.getChildren( "subtable", ns );
			for ( Element subtable : subtables ) {
				// name = subtable.getChild( "name", ns ).getText();
				folder = subtable.getChild( "folder", ns ).getText();
				cell = subtable.getChild( "foreignkeycell", ns ).getText();
				String tabfolder = "";
				String tabname = "";
				String tabdescription = "";
				String tabdescriptionProv = "";
				String cellname = "";
				String celldescription = "";

				// System.out.println( name + " - " + folder + " - " + cell );
				File fSubtable = new File( siardDatei.getAbsolutePath() + File.separator + "content"
						+ File.separator + "schema0" + File.separator + folder + File.separator + folder
						+ ".xml" );

				try {
					// grep "<c11>7561234567890</c11>" table13.xml >> output.txt
					String command = "cmd /c \"" + pathToGrepExe + " \"<" + cell + ">" + excerptString + "</"
							+ cell + ">\" " + fSubtable.getAbsolutePath() + " >> "
							+ tempOutFile.getAbsolutePath() + "\"";
					/* Das redirect Zeichen verunm�glicht eine direkte eingabe. mit dem geschachtellten Befehl
					 * gehts: cmd /c\"urspruenlicher Befehl\" */

					// System.out.println( command );

					Process proc = null;
					Runtime rt = null;

					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_ELEMENT_OPEN, folder ) );
					// TODO Start Wie maintable
					// Informationen zur Tabelle aus metadata.xml herausholen

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					// dbf.setValidating(false);
					DocumentBuilder db = dbf.newDocumentBuilder();
					org.w3c.dom.Document doc = db.parse( new FileInputStream( new File( siardDatei
							.getAbsolutePath() + File.separator + "header" + File.separator + "metadata.xml" ) ) );
					doc.getDocumentElement().normalize();

					dbf.setFeature( "http://xml.org/sax/features/namespaces", false );

					NodeList nlTable = doc.getElementsByTagName( "table" );
					for ( int i = 0; i < nlTable.getLength(); i++ ) {
						// f�r jede Tabelle (table) ...
						tabfolder = "";
						tabname = "";
						tabdescription = "";
						tabdescriptionProv = "";
						cellname = "";
						celldescription = "";

						Node nodenlTable = nlTable.item( i );
						NodeList childNodes = nodenlTable.getChildNodes();
						for ( int x = 0; x < childNodes.getLength(); x++ ) {
							Node subNodeI = childNodes.item( x );
							if ( subNodeI.getNodeName().equals( "folder" ) ) {
								// System.out.println( subNodeI.getTextContent() );
								if ( subNodeI.getTextContent().equals( folder ) ) {
									/* Es ist die richtige Tabelle. Ensprechend wird i ans ende Gestellt, damit die
									 * i-Schlaufe beendet wird. */
									tabfolder = subNodeI.getTextContent();
									i = nlTable.getLength();
								}
							} else if ( subNodeI.getNodeName().equals( "name" ) ) {
								tabname = subNodeI.getTextContent();
							} else if ( subNodeI.getNodeName().equals( "description" ) ) {
								tabdescriptionProv = new String( subNodeI.getTextContent().getBytes(), "UTF-8" );
								/* in der description generiert mit csv2siard wird nach "word" der Select Befehl
								 * angeh�ngt. Dieser soll nicht mit ausgegeben werden. */
								String word = "\\u000A";
								int endIndex = tabdescriptionProv.indexOf( word );
								tabdescription = tabdescriptionProv.substring( 0, endIndex );
							} else if ( subNodeI.getNodeName().equals( "columns" ) ) {
								NodeList childNodesColumns = subNodeI.getChildNodes();
								for ( int y = 0; y < childNodesColumns.getLength(); y++ ) {
									// f�r jede Zelle (column) ...
									Node subNodeII = childNodesColumns.item( y );
									NodeList childNodesColumn = subNodeII.getChildNodes();
									for ( int z = 0; z < childNodesColumn.getLength(); z++ ) {
										// f�r jedes Subelement der Zelle (name, description...) ...
										int cellNumber = (y + 1) / 2;
										// System.out.println( "Zelle Nr " + cellNumber );
										Node subNodeIII = childNodesColumn.item( z );
										if ( subNodeIII.getNodeName().equals( "name" ) ) {
											cellname = cellname + "<c" + cellNumber + ">" + subNodeIII.getTextContent()
													+ "</c" + cellNumber + ">";
											// System.out.println( cellname );
										} else if ( subNodeIII.getNodeName().equals( "description" ) ) {
											celldescription = celldescription + "<c" + cellNumber + ">"
													+ new String( subNodeIII.getTextContent().getBytes(), "UTF-8" ) + "</c"
													+ cellNumber + ">";
										}
									}
								}
							}
						}
						if ( i == nlTable.getLength() ) {
							// Ausgabe f�r jede Tabelle
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_TEXT, tabname, "tabname" ) );
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_TEXT, tabfolder, "tabfolder" ) );
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_TEXT, tabdescription,
											"tabdescription" ) );
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_TEXT, cellname, "name" ) );
							getMessageService().logError(
									getTextResourceService().getText( MESSAGE_XML_TEXT, celldescription,
											"description" ) );
						}
					}
					// TODO End Wie maintable

					try {
						Util.switchOffConsole();
						rt = Runtime.getRuntime();
						proc = rt.exec( command.toString().split( " " ) );
						// .split(" ") ist notwendig wenn in einem Pfad ein Doppelleerschlag vorhanden ist!

						// Fehleroutput holen
						StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR" );

						// Output holen
						StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT" );

						// Threads starten
						errorGobbler.start();
						outputGobbler.start();

						// Warte, bis wget fertig ist
						proc.waitFor();

						Util.switchOnConsole();

					} catch ( Exception e ) {
						getMessageService().logError(
								getTextResourceService().getText( MESSAGE_XML_MODUL_C )
										+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
						return false;
					} finally {
						if ( proc != null ) {
							closeQuietly( proc.getOutputStream() );
							closeQuietly( proc.getInputStream() );
							closeQuietly( proc.getErrorStream() );
						}
					}

					Scanner scanner = new Scanner( tempOutFile );
					content = "";
					try {
						content = scanner.useDelimiter( "\\Z" ).next();
					} catch ( Exception e ) {
						// Grep ergab kein treffer Content Null
						content = "";
					}
					scanner.close();

					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_ELEMENT_CONTENT, content ) );
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_ELEMENT_CLOSE, folder ) );

					if ( tempOutFile.exists() ) {
						Util.deleteDir( tempOutFile );
					}
					content = "";

					// Ende Grep

				} catch ( Exception e ) {
					getMessageService().logError(
							getTextResourceService().getText( MESSAGE_XML_MODUL_C )
									+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					return false;
				}

				// Ende SubTables
				if ( onWork == 41 ) {
					onWork = 2;
					System.out.print( "-   " );
					System.out.print( "\r" );
				} else if ( onWork == 11 ) {
					onWork = 12;
					System.out.print( "\\   " );
					System.out.print( "\r" );
				} else if ( onWork == 21 ) {
					onWork = 22;
					System.out.print( "|   " );
					System.out.print( "\r" );
				} else if ( onWork == 31 ) {
					onWork = 32;
					System.out.print( "/   " );
					System.out.print( "\r" );
				} else {
					onWork = onWork + 1;
				}
			}
			System.out.print( "   " );
			System.out.print( "\r" );
		} catch ( Exception e ) {
			getMessageService().logError(
					getTextResourceService().getText( MESSAGE_XML_MODUL_C )
							+ getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
			return false;
		}

		return isValid;
	}
}
