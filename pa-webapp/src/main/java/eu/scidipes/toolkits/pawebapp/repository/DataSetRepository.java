/*
 * Copyright (c) 2011-2014 Alliance for Permanent Access (APA) and its
 * contributors. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The APA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scidipes.toolkits.pawebapp.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import eu.scidipes.toolkits.palibrary.impl.DatasetRIL;
import eu.scidipes.toolkits.palibrary.impl.FormImpl;
import eu.scidipes.toolkits.palibrary.impl.FormsBundleImpl;
import eu.scidipes.toolkits.palibrary.impl.UploadRepInfoLabel;
import eu.scidipes.toolkits.palibrary.interfaces.Form;
import eu.scidipes.toolkits.palibrary.interfaces.FormsBundle;
import eu.scidipes.toolkits.pawebapp.model.DataSetCompletedFormCount;
import eu.scidipes.toolkits.pawebapp.model.FormCountByItemName;

/**
 * Customised Spring Data JPA repository to supplement predefined queries from {@link JpaRepository}
 *
 * @author Tom Bunting
 *
 */
public interface DataSetRepository extends JpaRepository<FormsBundleImpl, String> {

	/**
	 * Produces a <code>Set</code> of {@link DataSetCompletedFormCount} objects, each representing a data set and a
	 * count of its completed forms.
	 *
	 * @return a set of data set completed form counts
	 */
	@Query("SELECT NEW eu.scidipes.toolkits.pawebapp.model.DataSetCompletedFormCount(fb, COUNT(f)) "
			+ "FROM FormsBundleImpl fb LEFT JOIN fb.forms f "
			+ "ON (f.dataHolder IS NOT NULL) AND (f.dataHolder <> '') GROUP BY fb")
	Set<DataSetCompletedFormCount> findAllWithCompleteFormsCount();

	@Query("SELECT COUNT(fb) FROM FormsBundleImpl fb WHERE fb.bundleName = TRIM(:bundleName)")
	Long countDatasetsByBundle(@Param("bundleName") String bundleName);

	/**
	 * Produces a <code>Set</code> of {@link FormsBundle} objects each of which has at least one completed {@link Form}
	 *
	 * @return a set of forms that are ready for preservation
	 */
	@Query("SELECT fb FROM FormsBundleImpl fb INNER JOIN fb.forms f "
			+ "ON (f.dataHolder IS NOT NULL) AND (f.dataHolder <> '') ORDER BY fb.displayName")
	Set<FormsBundle> findAllPreservable();

	@Query("SELECT NEW eu.scidipes.toolkits.pawebapp.model.FormCountByItemName(f.groupOrder, f.group, f.name, f.displayName, COUNT(f.dataHolder)) "
			+ "FROM FormImpl f WHERE f.parentBundle.datasetName = :datasetName "
			+ "GROUP BY f.groupOrder, f.group, f.name, f.displayName ORDER BY f.groupOrder, f.group, f.name")
	Set<FormCountByItemName> findDistinctFormsByBundle(@Param("datasetName") String datasetName);

	@Query("SELECT f FROM FormImpl f WHERE f.parentBundle.datasetName = :datasetName "
			+ "ORDER BY f.groupOrder, f.group, f.name")
	List<Form> findAllOrderByGrpItemName(@Param("datasetName") String datasetName);

	@Query("SELECT f FROM FormImpl f WHERE f.parentBundle.datasetName = :datasetName AND TRIM(f.name) = TRIM(:formName)")
	Form findOneByName(@Param("datasetName") String datasetName, @Param("formName") String formName);

	@Query("SELECT f FROM FormImpl f WHERE f.parentBundle.datasetName = :datasetName AND TRIM(f.name) = TRIM(:formName) "
			+ "ORDER BY f.groupOrder, f.group, f.name")
	List<Form> findAllByName(@Param("datasetName") String datasetName, @Param("formName") String formName);

	@Query("SELECT f FROM FormImpl f WHERE f.formID = :formID")
	FormImpl findOneForm(@Param("formID") Integer formID);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE DatasetRIL d SET d.ril = :ril WHERE d.rilCPID = :rilCPID")
	@Transactional
	void updateDatasetRIL(@Param("ril") UploadRepInfoLabel ril, @Param("rilCPID") String rilCPID);

	@Query("SELECT d from DatasetRIL d WHERE d.rilCPID = :rilCPID")
	DatasetRIL findOneRIL(@Param("rilCPID") String rilCPID);

}
