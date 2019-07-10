/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.isanteplusconceptmetadata;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.metadatasharing.ImportConfig;
import org.openmrs.module.metadatasharing.ImportMode;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.module.metadatasharing.wrapper.PackageImporter;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class ConceptmetadataActivator implements ModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
		
	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing openmrs-module-isanteplus-conceptmetadata Module");
	}
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("openmrs-module-isanteplus-conceptmetadata Module refreshed");
	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting openmrs-module-isanteplus-conceptmetadata Module");
	}
	
	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		//check if the default user responsible for setting up metadata is set
		try {
			installMetadataPackageIfNecessary(IsanteplusMetadataUtils._PackageUuids.METADATA_GROUPING_CONCEPTS_UUID, IsanteplusMetadataUtils._PackageNames.METADATA_GROUPING_CONCEPTS);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to setup initial data", ex);
		}
		log.info("openmrs-module-isanteplus-conceptmetadata Module started");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping openmrs-module-isanteplus-conceptmetadata Module");
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("openmrs-module-isanteplus-conceptmetadata Module stopped");
	}
	
	/**
	 * Checks whether the given version of the MDS package has been installed yet,
	 * and if not, install it
	 *
	 * @param groupUuid
	 * @param filename
	 * @return whether any changes were made to the db
	 * @throws IOException
	 */
	private boolean installMetadataPackageIfNecessary(String groupUuid, String filename) throws IOException {
		try {
			Matcher matcher = Pattern.compile("\\w+-(\\d+).zip").matcher(filename);

			if (!matcher.matches())
				throw new RuntimeException("Filename must match PackageNameWithNoSpaces-1.zip");
			Integer version = Integer.valueOf(matcher.group(1));

			ImportedPackage installed = Context.getService(MetadataSharingService.class)
					.getImportedPackageByGroup(groupUuid);
			if (installed != null && installed.getVersion() >= version) {
				log.info("Metadata package " + filename + " is already installed with version "
						+ installed.getVersion());
				return false;
			}

			if (getClass().getClassLoader().getResource(filename) == null) {
				throw new RuntimeException("Cannot find " + filename + " for group " + groupUuid
						+ ". Make sure it's in api/src/main/resources");
			}

			PackageImporter metadataImporter = MetadataSharing.getInstance().newPackageImporter();
			metadataImporter.setImportConfig(ImportConfig.valueOf(ImportMode.MIRROR));
			metadataImporter.loadSerializedPackageStream(getClass().getClassLoader().getResourceAsStream(filename));
			metadataImporter.importPackage();
			return true;
		} catch (Exception ex) {
			log.error("Failed to install metadata package " + filename, ex);
			return false;
		}
	}

}
