package ca.uhn.fhir.jpa.dao.data;

/*-
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.mdm.api.MdmMatchResultEnum;
import ca.uhn.fhir.jpa.entity.MdmLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMdmLinkDao extends JpaRepository<MdmLink, Long> {
	@Modifying
	@Query("DELETE FROM MdmLink f WHERE myGoldenResourcePid = :pid OR mySourcePid = :pid")
	int deleteWithAnyReferenceToPid(@Param("pid") Long thePid);

	@Modifying
	@Query("DELETE FROM MdmLink f WHERE (myGoldenResourcePid = :pid OR mySourcePid = :pid) AND myMatchResult <> :matchResult")
	int deleteWithAnyReferenceToPidAndMatchResultNot(@Param("pid") Long thePid, @Param("matchResult") MdmMatchResultEnum theMatchResult);

	@Query("SELECT ml2.myGoldenResourcePid, ml2.mySourcePid FROM MdmLink ml2 " +
		"WHERE ml2.myMatchResult=:matchResult " +
		"AND ml2.myGoldenResourcePid IN (" +
			"SELECT ml.myGoldenResourcePid FROM MdmLink ml " +
			"INNER JOIN ResourceLink hrl " +
			"ON hrl.myTargetResourcePid=ml.mySourcePid " +
			"AND hrl.mySourceResourcePid=:groupPid " +
			"AND hrl.mySourcePath='Group.member.entity' " +
			"AND hrl.myTargetResourceType='Patient'" +
		")")
	List<List<Long>> expandPidsFromGroupPidGivenMatchResult(@Param("groupPid") Long theGroupPid, @Param("matchResult") MdmMatchResultEnum theMdmMatchResultEnum);
}
