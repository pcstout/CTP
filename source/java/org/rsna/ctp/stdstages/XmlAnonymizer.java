/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.ctp.stdstages;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.rsna.ctp.objects.FileObject;
import org.rsna.ctp.objects.XmlObject;
import org.rsna.ctp.pipeline.AbstractPipelineStage;
import org.rsna.ctp.pipeline.Processor;
import org.rsna.ctp.stdstages.anonymizer.AnonymizerStatus;
import org.rsna.ctp.stdstages.anonymizer.xml.XMLAnonymizer;
import org.rsna.util.FileUtil;
import org.w3c.dom.Element;

/**
 * The XmlAnonymizer pipeline stage class.
 */
public class XmlAnonymizer extends AbstractPipelineStage implements Processor, Scriptable {

	static final Logger logger = Logger.getLogger(XmlAnonymizer.class);

	public File scriptFile = null;

	/**
	 * Construct the XmlAnonymizer PipelineStage.
	 * @param element the XML element from the configuration file
	 * specifying the configuration of the stage.
	 */
	public XmlAnonymizer(Element element) {
		super(element);
		scriptFile = FileUtil.getFile(element.getAttribute("script").trim(), "examples/example-xml-anonymizer.script");
	}

	/**
	 * Process an XmlObject, logging the filename and returning the processed object.
	 * If there is no script file, pass the object unmodified.
	 * If the object is not an XmlObject, pass the object unmodified.
	 * @param fileObject the object to process.
	 * @return the processed FileObject.
	 */
	public FileObject process(FileObject fileObject) {
		lastFileIn = new File(fileObject.getFile().getAbsolutePath());
		lastTimeIn = System.currentTimeMillis();

		if ( (fileObject instanceof XmlObject) && (scriptFile != null) ) {
			File file = fileObject.getFile();
			AnonymizerStatus status = XMLAnonymizer.anonymize(file,file,scriptFile);
			if (status.isOK()) {
				fileObject = FileObject.getInstance(file);
			}
			else if (status.isQUARANTINE()) {
				if (quarantine != null) quarantine.insert(fileObject);
				lastFileOut = null;
				lastTimeOut = System.currentTimeMillis();
				return null;
			}
			else if (status.isSKIP()) ; //keep the input object
		}

		lastFileOut = new File(fileObject.getFile().getAbsolutePath());
		lastTimeOut = System.currentTimeMillis();
		return fileObject;
	}

	/**
	 * Get the script file.
	 * @return the script file used by this stage.
	 */
	public File[] getScriptFiles() {
		return new File[] {scriptFile};
	}
}