/*
* JasperReports - Free Java Reporting Library.
* Copyright (C) 2001 - 2012 Jaspersoft Corporation. All rights reserved.
* http://www.jaspersoft.com.
*
* Unless you have purchased a commercial license agreement from Jaspersoft,
* the following license terms apply:
*
* This program is part of JasperReports.
*
* JasperReports is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* JasperReports is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
*/

package com.jaspersoft.jasperserver.jaxrs;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.remote.common.CallTemplate;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceWrapper;
import com.jaspersoft.jasperserver.remote.exception.RemoteException;
import com.jaspersoft.jasperserver.search.common.ResourceDetails;
import com.jaspersoft.jasperserver.search.filter.FolderFilter;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
import com.jaspersoft.jasperserver.search.service.impl.RepositorySearchCriteriaImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of REST service for JasperServer
 * This service will search only for report resources in repository.
 *
 * @author ElChubarone
 */
@Service
@Path("/search")
@CallTemplate(RepoServiceCallTemplate.class)
public class SearchJaxrsService extends RemoteServiceWrapper<RepositorySearchService> {

    @Resource
    private FolderFilter folderFilter;

    @Resource(name = "repositorySearchService")
    public void setRemoteService(RepositorySearchService remoteService) {
        this.remoteService = remoteService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response search(@QueryParam("query") @DefaultValue("") final String queryString,
                           @QueryParam("start") @DefaultValue("0") final int startIndex,
                           @QueryParam("max") @DefaultValue("100") final int maxItems) {

        return callRemoteService(new ConcreteCaller<Response>() {
            public Response call(RepositorySearchService service) throws RemoteException {
                final RepositorySearchCriteria repositorySearchCriteria = new RepositorySearchCriteriaImpl.Builder().setFolderUri("/reports/samples")
                        .setSearchText(queryString).setStartIndex(startIndex).setMaxCount(maxItems)
                        .setSearchMode(SearchMode.SEARCH).setSortBy("name").getCriteria();

                repositorySearchCriteria.setResourceTypes(new ArrayList<String>());
                repositorySearchCriteria.getResourceTypes().add("com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit");

                final List<ResourceDetails> results = service.getResults(ExecutionContextImpl.getRuntimeExecutionContext(), repositorySearchCriteria);

                return Response.ok(results).build();
            }
        });
    }
}
