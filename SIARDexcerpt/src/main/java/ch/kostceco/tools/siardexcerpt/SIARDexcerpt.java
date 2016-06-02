/* == SIARDexcerpt ==============================================================================
 * The SIARDexcerpt v0.0.4 application is used for excerpt a record from a SIARD-File. Copyright (C)
 * 2016 Claire R�thlisberger (KOST-CECO)
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

package ch.kostceco.tools.siardexcerpt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.kostceco.tools.siardexcerpt.controller.Controllerexcerpt;
import ch.kostceco.tools.siardexcerpt.logging.LogConfigurator;
import ch.kostceco.tools.siardexcerpt.logging.Logger;
import ch.kostceco.tools.siardexcerpt.logging.MessageConstants;
import ch.kostceco.tools.siardexcerpt.service.ConfigurationService;
import ch.kostceco.tools.siardexcerpt.service.TextResourceService;
import ch.kostceco.tools.siardexcerpt.util.Util;

/** Dies ist die Starter-Klasse, verantwortlich f�r das Initialisieren des Controllers, des Loggings
 * und das Parsen der Start-Parameter.
 * 
 * @author Rc Claire R�thlisberger, KOST-CECO */

public class SIARDexcerpt implements MessageConstants
{

	private static final Logger		LOGGER	= new Logger( SIARDexcerpt.class );

	private TextResourceService		textResourceService;
	private ConfigurationService	configurationService;

	public TextResourceService getTextResourceService()
	{
		return textResourceService;
	}

	public void setTextResourceService( TextResourceService textResourceService )
	{
		this.textResourceService = textResourceService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	/** Die Eingabe besteht aus mind 3 Parameter: [0] Pfad zur SIARD-Datei oder Verzeichnis [1]
	 * configfile [2] Modul
	 * 
	 * �bersicht der Module: --init --search --extract sowie --finish
	 * 
	 * bei --search kommen danach noch die Suchtexte und bei --extract die Schl�ssel
	 * 
	 * @param args
	 * @throws IOException */

	public static void main( String[] args ) throws IOException
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:config/applicationContext.xml" );

		/** SIARDexcerpt: Aufbau des Tools
		 * 
		 * 1) init: Config Kopieren und ggf SIARD-Datei ins Workverzeichnis entpacken
		 * 
		 * 2) search: gem�ss config die Tabelle mit Suchtext befragen und Ausgabe des Resultates
		 * 
		 * 3) extract: mit den Keys anhand der config einen Records herausziehen und anzeigen
		 * 
		 * 4) finish: Config-Kopie sowie Workverzeichnis l�schen */

		/* TODO: siehe Bemerkung im applicationContext-services.xml bez�glich Injection in der
		 * Superklasse aller Impl-Klassen ValidationModuleImpl validationModuleImpl =
		 * (ValidationModuleImpl) context.getBean("validationmoduleimpl"); */

		SIARDexcerpt siardexcerpt = (SIARDexcerpt) context.getBean( "siardexcerpt" );

		// Ist die Anzahl Parameter (mind 3) korrekt?
		if ( args.length < 3 ) {
			System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
			System.exit( 1 );
		}

		String module = new String( args[2] );
		File siardDatei = new File( args[0] );
		File configFile = new File( args[1] );

		/* arg 1 gibt den Pfad zur configdatei an. Da dieser in ConfigurationServiceImpl hartcodiert
		 * ist, wird diese nach "configuration/SIARDexcerpt.conf.xml" kopiert. */
		File configFileHard = new File( "configuration" + File.separator + "SIARDexcerpt.conf.xml" );

		// excerpt ist der Standardwert wird aber anhand der config dann gesetzt
		File directoryOfOutput = new File( "excerpt" );

		// temp_SIARDexcerpt ist der Standardwert wird aber anhand der config dann gesetzt
		File tmpDir = new File( "temp_SIARDexcerpt" );

		boolean okA = false;
		boolean okB = false;
		boolean okC = false;

		// die Anwendung muss mindestens unter Java 6 laufen
		String javaRuntimeVersion = System.getProperty( "java.vm.version" );
		if ( javaRuntimeVersion.compareTo( "1.6.0" ) < 0 ) {
			System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_WRONG_JRE ) );
			System.exit( 1 );
		}

		if ( module.equalsIgnoreCase( "--init" ) ) {

			/** 1) init: Config Kopieren und ggf SIARD-Datei ins Workverzeichnis entpacken
			 * 
			 * a) config muss existieren und SIARDexcerpt.conf.xml noch nicht
			 * 
			 * b) Excerptverzeichnis mit schreibrechte und ggf anlegen
			 * 
			 * c) Workverzeichnis muss leer sein und mit schreibrechte
			 * 
			 * d) SIARD-Datei entpacken
			 * 
			 * e) Struktur-Check SIARD-Verzeichnis
			 * 
			 * TODO: Erledigt */

			System.out.println( "SIARDexcerpt: init" );

			/** a) config muss existieren und SIARDexcerpt.conf.xml noch nicht */
			if ( !configFile.exists() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_CONFIGFILE_FILENOTEXISTING, configFile.getAbsolutePath() ) );
				System.exit( 1 );
			}

			if ( configFileHard.exists() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_CONFIGFILEHARD_FILEEXISTING ) );
				System.exit( 1 );
			}
			Util.copyFile( configFile, configFileHard );

			/** b) Excerptverzeichnis mit schreibrechte und ggf anlegen */
			String pathToOutput = siardexcerpt.getConfigurationService().getPathToOutput();

			directoryOfOutput = new File( pathToOutput );

			if ( !directoryOfOutput.exists() ) {
				directoryOfOutput.mkdir();
			}

			// Im Logverzeichnis besteht kein Schreibrecht
			if ( !directoryOfOutput.canWrite() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_LOGDIRECTORY_NOTWRITABLE, directoryOfOutput ) );
				// L�schen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			if ( !directoryOfOutput.isDirectory() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_LOGDIRECTORY_NODIRECTORY ) );
				// L�schen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			/** c) Workverzeichnis muss leer sein und mit schreibrechte */
			String pathToWorkDir = siardexcerpt.getConfigurationService().getPathToWorkDir();

			tmpDir = new File( pathToWorkDir );

			/* bestehendes Workverzeichnis Abbruch wenn nicht leer, da am Schluss das Workverzeichnis
			 * gel�scht wird und entsprechend bestehende Dateien gel�scht werden k�nnen */
			if ( tmpDir.exists() ) {
				if ( tmpDir.isDirectory() ) {
					// Get list of file in the directory. When its length is not zero the folder is not empty.
					String[] files = tmpDir.list();
					if ( files.length > 0 ) {
						System.out.println( siardexcerpt.getTextResourceService().getText(
								ERROR_WORKDIRECTORY_EXISTS, pathToWorkDir ) );
						// L�schen des configFileHard, falls eines angelegt wurde
						if ( configFileHard.exists() ) {
							Util.deleteDir( configFileHard );
						}
						System.exit( 1 );
					}
				}
			}
			if ( !tmpDir.exists() ) {
				tmpDir.mkdir();
			}

			// Im Workverzeichnis besteht kein Schreibrecht
			if ( !tmpDir.canWrite() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_WORKDIRECTORY_NOTWRITABLE, pathToWorkDir ) );
				// L�schen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			/** d) SIARD-Datei entpacken */
			if ( !siardDatei.exists() ) {
				// SIARD-Datei existiert nicht
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_SIARDFILE_FILENOTEXISTING, siardDatei.getAbsolutePath() ) );
				// L�schen des configFileHard, falls eines angelegt wurde
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				System.exit( 1 );
			}

			if ( !siardDatei.isDirectory() ) {

				/* SIARD-Datei ist eine Datei
				 * 
				 * Die Datei muss ins Workverzeichnis extrahiert werden. Dies erfolgt im Modul A.
				 * 
				 * danach der Pfad zu SIARD-Datei dorthin zeigen lassen */

				Controllerexcerpt controllerexcerpt = (Controllerexcerpt) context
						.getBean( "controllerexcerpt" );
				File siardDateiNew = new File( pathToWorkDir + File.separator + siardDatei.getName() );
				okA = controllerexcerpt.executeA( siardDatei, siardDateiNew, "" );

				if ( !okA ) {
					// SIARD Datei konte nicht entpackt werden
					System.out.println( MESSAGE_XML_MODUL_A );
					System.out.println( ERROR_XML_A_CANNOTEXTRACTZIP );

					// L�schen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
					if ( tmpDir.exists() ) {
						Util.deleteDir( tmpDir );
					}
					if ( configFileHard.exists() ) {
						Util.deleteDir( configFileHard );
					}
					// Fehler Extraktion --> invalide
					System.exit( 2 );
				} else {
					@SuppressWarnings("unused")
					File siardDateiOld = siardDatei;
					siardDatei = siardDateiNew;
				}

			} else {
				/* SIARD-Datei entpackt oder Datei war bereits ein Verzeichnis.
				 * 
				 * Gerade bei gr�sseren SIARD-Dateien ist es sinnvoll an einer Stelle das ausgepackte SIARD
				 * zu haben, damit diese nicht immer noch extrahiert werden muss */
			}

			/** e) Struktur-Check SIARD-Verzeichnis */
			File content = new File( siardDatei.getAbsolutePath() + File.separator + "content" );
			File header = new File( siardDatei.getAbsolutePath() + File.separator + "header" );
			File xsd = new File( siardDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "metadata.xsd" );
			File metadata = new File( siardDatei.getAbsolutePath() + File.separator + "header"
					+ File.separator + "metadata.xml" );

			if ( !content.exists() || !header.exists() || !xsd.exists() || !metadata.exists() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_XML_B_STRUCTURE ) );
				// L�schen des Arbeitsverzeichnisses und configFileHard, falls eines angelegt wurde
				if ( tmpDir.exists() ) {
					Util.deleteDir( tmpDir );
				}
				if ( configFileHard.exists() ) {
					Util.deleteDir( configFileHard );
				}
				// Fehler Extraktion --> invalide
				System.exit( 2 );
			} else {
				// Struktur sieht plausibel aus, extraktion kann starten
			}

		} // End init

		if ( module.equalsIgnoreCase( "--search" ) ) {

			/** 2) search: gem�ss config die Tabelle mit Suchtext befragen und Ausgabe des Resultates
			 * 
			 * a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext
			 * 
			 * b) Suchtext einlesen
			 * 
			 * c) search.xml vorbereiten (Header) und xsl in Output kopieren
			 * 
			 * d) grep ausf�hren
			 * 
			 * e) Suchergebnis speichern und anzeigen (via GUI)
			 * 
			 * TODO: Noch offen */

			System.out.println( "SIARDexcerpt: search" );

			String pathToOutput = siardexcerpt.getConfigurationService().getPathToOutput();

			directoryOfOutput = new File( pathToOutput );

			if ( !directoryOfOutput.exists() ) {
				directoryOfOutput.mkdir();
			}

			/** a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext */
			if ( args.length < 4 ) {
				System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
				System.exit( 1 );
			}

			if ( !siardDatei.isDirectory() ) {
				File siardDateiNew = new File( tmpDir.getAbsolutePath() + File.separator
						+ siardDatei.getName() );
				if ( !siardDateiNew.exists() ) {
					System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_NOINIT ) );
					System.exit( 1 );
				} else {
					siardDatei = siardDateiNew;
				}
			}

			/** b) Suchtext einlesen */
			String searchString = new String( args[3] );

			/** c) search.xml vorbereiten (Header) und xsl in Output kopieren */
			// Zeitstempel der Datenextraktion
			java.util.Date nowStartS = new java.util.Date();
			java.text.SimpleDateFormat sdfStartS = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
			String ausgabeStartS = sdfStartS.format( nowStartS );

			/* Der SearchString kann zeichen enthalten, welche nicht im Dateinamen vorkommen d�rfen.
			 * Entsprechend werden diese normalisiert */
			String searchStringFilename = searchString.replaceAll( "/", "_" );
			searchStringFilename = searchStringFilename.replaceAll( ">", "_" );
			searchStringFilename = searchStringFilename.replaceAll( "<", "_" );
			searchStringFilename = searchStringFilename.replace( ".*", "_" );
			searchStringFilename = searchStringFilename.replaceAll( "___", "_" );
			searchStringFilename = searchStringFilename.replaceAll( "__", "_" );

			String outDateiNameS = siardDatei.getName() + "_" + searchStringFilename + "_SIARDsearch.xml";
			outDateiNameS = outDateiNameS.replaceAll( "__", "_" );

			// Informationen zum Archiv holen
			String archiveS = siardexcerpt.getConfigurationService().getArchive();

			// Konfiguration des Outputs, ein File Logger wird zus�tzlich erstellt
			LogConfigurator logConfiguratorS = (LogConfigurator) context.getBean( "logconfigurator" );
			String outFileNameS = logConfiguratorS.configure( directoryOfOutput.getAbsolutePath(),
					outDateiNameS );
			File outFileSearch = new File( outFileNameS );
			// Ab hier kann ins Output geschrieben werden...

			// Informationen zum XSL holen
			String pathToXSLS = siardexcerpt.getConfigurationService().getPathToXSLsearch();

			File xslOrigS = new File( pathToXSLS );
			File xslCopyS = new File( directoryOfOutput.getAbsolutePath() + File.separator
					+ xslOrigS.getName() );
			if ( !xslCopyS.exists() ) {
				Util.copyFile( xslOrigS, xslCopyS );
			}

			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_HEADER,
					xslCopyS.getName() ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_START,
					ausgabeStartS ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_TEXT, archiveS,
					"Archive" ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_INFO ) );

			/** d) search: dies ist in einem eigenen Modul realisiert */
			Controllerexcerpt controllerexcerptS = (Controllerexcerpt) context
					.getBean( "controllerexcerpt" );

			okB = controllerexcerptS.executeB( siardDatei, outFileSearch, searchString );

			/** e) Ausgabe und exitcode */
			if ( !okB ) {
				// Suche konnte nicht erfolgen
				LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_MODUL_B ) );
				LOGGER.logError( siardexcerpt.getTextResourceService().getText(
						ERROR_XML_B_CANNOTSEARCHRECORD ) );
				LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				System.out.println( MESSAGE_XML_MODUL_B );
				System.out.println( ERROR_XML_B_CANNOTSEARCHRECORD );
				System.out.println( "" );

				// L�schen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Fehler Extraktion --> invalide
				System.exit( 2 );
			} else {
				// Suche konnte durchgef�hrt werden

				LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );

				// Die Konfiguration hereinkopieren
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating( false );

					factory.setExpandEntityReferences( false );

					Document docConfig = factory.newDocumentBuilder().parse( configFile );
					NodeList list = docConfig.getElementsByTagName( "configuration" );
					Element element = (Element) list.item( 0 );

					Document docLog = factory.newDocumentBuilder().parse( outFileSearch );

					Node dup = docLog.importNode( element, true );

					docLog.getDocumentElement().appendChild( dup );
					FileWriter writer = new FileWriter( outFileSearch );

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ElementToStream( docLog.getDocumentElement(), baos );
					String stringDoc2 = new String( baos.toByteArray() );
					writer.write( stringDoc2 );
					writer.close();

					// Der Header wird dabei leider verschossen, wieder zur�ck �ndern
					String newstring = siardexcerpt.getTextResourceService().getText( MESSAGE_XML_HEADER,
							xslCopyS.getName() );
					String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>";
					Util.oldnewstring( oldstring, newstring, outFileSearch );

				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ siardexcerpt.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					System.out.println( "Exception: " + e.getMessage() );
				}

				// L�schen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Record konnte extrahiert werden
				System.exit( 0 );
			}

		} // End search

		if ( module.equalsIgnoreCase( "--excerpt" ) ) {

			/** 3) extract: mit den Keys anhand der config einen Records herausziehen und anzeigen
			 * 
			 * a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext
			 * 
			 * b) extract.xml vorbereiten (Header) und xsl in Output kopieren
			 * 
			 * c) extraktion: dies ist in einem eigenen Modul realisiert
			 * 
			 * d) Ausgabe und exitcode
			 * 
			 * TODO: Erledigt */

			System.out.println( "SIARDexcerpt: extract" );

			String pathToOutput = siardexcerpt.getConfigurationService().getPathToOutput();

			directoryOfOutput = new File( pathToOutput );

			if ( !directoryOfOutput.exists() ) {
				directoryOfOutput.mkdir();
			}

			/** a) Ist die Anzahl Parameter (mind 4) korrekt? arg4 = Suchtext */
			if ( args.length < 4 ) {
				System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_PARAMETER_USAGE ) );
				System.exit( 1 );
			}

			if ( !siardDatei.isDirectory() ) {
				File siardDateiNew = new File( tmpDir.getAbsolutePath() + File.separator
						+ siardDatei.getName() );
				if ( !siardDateiNew.exists() ) {
					System.out.println( siardexcerpt.getTextResourceService().getText( ERROR_NOINIT ) );
					System.exit( 1 );
				} else {
					siardDatei = siardDateiNew;
				}
			}

			/** b) extract.xml vorbereiten (Header) und xsl in Output kopieren */
			// Zeitstempel der Datenextraktion
			java.util.Date nowStart = new java.util.Date();
			java.text.SimpleDateFormat sdfStart = new java.text.SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
			String ausgabeStart = sdfStart.format( nowStart );

			String excerptString = new String( args[3] );
			String outDateiName = siardDatei.getName() + "_" + excerptString + "_SIARDexcerpt.xml";

			// Informationen zum Archiv holen
			String archive = siardexcerpt.getConfigurationService().getArchive();

			// Konfiguration des Outputs, ein File Logger wird zus�tzlich erstellt
			LogConfigurator logConfigurator = (LogConfigurator) context.getBean( "logconfigurator" );
			String outFileName = logConfigurator.configure( directoryOfOutput.getAbsolutePath(),
					outDateiName );
			File outFile = new File( outFileName );
			// Ab hier kann ins Output geschrieben werden...

			// Informationen zum XSL holen
			String pathToXSL = siardexcerpt.getConfigurationService().getPathToXSL();

			File xslOrig = new File( pathToXSL );
			if ( !xslOrig.exists() ) {
				System.out.println( siardexcerpt.getTextResourceService().getText(
						ERROR_CONFIGFILE_FILENOTEXISTING, pathToXSL ) );
				System.exit( 1 );
			}
			File xslCopy = new File( directoryOfOutput.getAbsolutePath() + File.separator
					+ xslOrig.getName() );
			if ( !xslCopy.exists() ) {
				Util.copyFile( xslOrig, xslCopy );
			}

			// Information aus metadata holen
			String dbname = "";
			String dataOriginTimespan = "";
			String dbdescription = "";
			String keyexcerpt = "";

			try {

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				// dbf.setValidating(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( new FileInputStream( new File( siardDatei.getAbsolutePath()
						+ File.separator + "header" + File.separator + "metadata.xml" ) ) );
				doc.getDocumentElement().normalize();

				dbf.setFeature( "http://xml.org/sax/features/namespaces", false );

				NodeList nlDbname = doc.getElementsByTagName( "dbname" );
				Node nodeDbname = nlDbname.item( 0 );
				dbname = nodeDbname.getTextContent();

				NodeList nlDataOriginTimespan = doc.getElementsByTagName( "dataOriginTimespan" );
				Node nodeDataOriginTimespan = nlDataOriginTimespan.item( 0 );
				dataOriginTimespan = nodeDataOriginTimespan.getTextContent();

				NodeList nlSiardArchive = doc.getElementsByTagName( "siardArchive" );
				Node nodeSiardArchive = nlSiardArchive.item( 0 );
				NodeList childNodes = nodeSiardArchive.getChildNodes();
				for ( int x = 0; x < childNodes.getLength(); x++ ) {
					Node subNode = childNodes.item( x );
					if ( subNode.getNodeName().equals( "description" ) ) {
						dbdescription = new String( subNode.getTextContent().getBytes(), "UTF-8");
						// TODO: leider funktioniert das encoding nicht :-(
					}
				}

				String primarykeyname = siardexcerpt.getConfigurationService().getMaintablePrimarykeyName();
				keyexcerpt = primarykeyname + " = " + excerptString;

			} catch ( Exception e ) {
				LOGGER.logError( "<Error>"
						+ siardexcerpt.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
				System.out.println( "Exception: " + e.getMessage() );
			}

			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_HEADER,
					xslCopy.getName() ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_START,
					ausgabeStart ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_TEXT, archive,
					"Archive" ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_TEXT, dbname,
					"dbname" ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_TEXT,
					dataOriginTimespan, "dataOriginTimespan" ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_TEXT,
					dbdescription, "dbdescription" ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_TEXT, keyexcerpt,
					"keyexcerpt" ) );
			LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_INFO ) );

			/** c) extraktion: dies ist in einem eigenen Modul realisiert */
			Controllerexcerpt controllerexcerpt = (Controllerexcerpt) context
					.getBean( "controllerexcerpt" );

			okC = controllerexcerpt.executeC( siardDatei, outFile, excerptString );

			/** d) Ausgabe und exitcode */
			if ( !okC ) {
				// Record konnte nicht extrahiert werden
				LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_MODUL_C ) );
				LOGGER.logError( siardexcerpt.getTextResourceService().getText(
						ERROR_XML_C_CANNOTEXTRACTRECORD ) );
				LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );
				System.out.println( MESSAGE_XML_MODUL_C );
				System.out.println( ERROR_XML_C_CANNOTEXTRACTRECORD );
				System.out.println( "" );

				// L�schen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Fehler Extraktion --> invalide
				System.exit( 2 );
			} else {
				// Record konnte extrahiert werden
				LOGGER.logError( siardexcerpt.getTextResourceService().getText( MESSAGE_XML_LOGEND ) );

				// Die Konfiguration hereinkopieren
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating( false );

					factory.setExpandEntityReferences( false );

					Document docConfig = factory.newDocumentBuilder().parse( configFile );
					NodeList list = docConfig.getElementsByTagName( "configuration" );
					Element element = (Element) list.item( 0 );

					Document docLog = factory.newDocumentBuilder().parse( outFile );

					Node dup = docLog.importNode( element, true );

					docLog.getDocumentElement().appendChild( dup );
					FileWriter writer = new FileWriter( outFile );

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ElementToStream( docLog.getDocumentElement(), baos );
					String stringDoc2 = new String( baos.toByteArray() );
					writer.write( stringDoc2 );
					writer.close();

					// Der Header wird dabei leider verschossen, wieder zur�ck �ndern
					String newstring = siardexcerpt.getTextResourceService().getText( MESSAGE_XML_HEADER,
							xslCopy.getName() );
					String oldstring = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>";
					Util.oldnewstring( oldstring, newstring, outFile );

				} catch ( Exception e ) {
					LOGGER.logError( "<Error>"
							+ siardexcerpt.getTextResourceService().getText( ERROR_XML_UNKNOWN, e.getMessage() ) );
					System.out.println( "Exception: " + e.getMessage() );
				}

				// L�schen des Arbeitsverzeichnisses und configFileHard erfolgt erst bei schritt 4 finish

				// Record konnte extrahiert werden
				System.exit( 0 );

			}

		} // End extract

		if ( module.equalsIgnoreCase( "--finish" ) ) {

			/** 4) finish: Config-Kopie sowie Workverzeichnis l�schen
			 * 
			 * TODO: Erledigt */

			System.out.println( "SIARDexcerpt: finish" );

			// L�schen des Arbeitsverzeichnisses und confiFileHard, falls eines angelegt wurde
			if ( tmpDir.exists() ) {
				Util.deleteDir( tmpDir );
			}
			if ( configFileHard.exists() ) {
				Util.deleteDir( configFileHard );
			}

		} // End finish

	}

	public static void ElementToStream( Element element, OutputStream out )
	{
		try {
			DOMSource source = new DOMSource( element );
			StreamResult result = new StreamResult( out );
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.transform( source, result );
		} catch ( Exception ex ) {
		}
	}

}
