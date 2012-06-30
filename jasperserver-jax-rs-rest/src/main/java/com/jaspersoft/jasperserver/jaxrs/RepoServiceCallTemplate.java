/*
* Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights  reserved.
* http://www.jaspersoft.com.
*
* Unless you have purchased  a commercial license agreement from Jaspersoft,
* the following license terms  apply:
*
* This program is free software: you can redistribute it and/or  modify
* it under the terms of the GNU Affero General Public License  as
* published by the Free Software Foundation, either version 3 of  the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero  General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public  License
* along with this program.&nbsp; If not, see <http://www.gnu.org/licenses/>.
*/
package com.jaspersoft.jasperserver.jaxrs;

import com.jaspersoft.jasperserver.api.JSValidationException;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceCallTemplate;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceInTemplateCaller;
import com.jaspersoft.jasperserver.remote.exception.RemoteException;
import com.jaspersoft.jasperserver.remote.exception.ResourceAlreadyExistsException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.xml.ErrorDescriptor;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


/**
 * @author ElChubarone
 */
public class RepoServiceCallTemplate implements RemoteServiceCallTemplate<RepositorySearchService> {
    private static final Log log = LogFactory.getLog(RepoServiceCallTemplate.class);
    @SuppressWarnings("unchecked")
    public <ResponseType> ResponseType callRemoteService(RemoteServiceInTemplateCaller<ResponseType, RepositorySearchService> caller, RepositorySearchService service) {
        Response response;
        try {
            response = (Response)caller.call(service);
        }catch (JSValidationException e){
            response = Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build();
        }catch (ResourceNotFoundException e) {
            response = Response.status(Response.Status.NOT_FOUND).entity(e.getErrorDescriptor()).build();
        }catch (ResourceAlreadyExistsException e){
            response = Response.status(Response.Status.FORBIDDEN).entity(e.getErrorDescriptor()).build();
        }catch (JSResourceNotFoundException e){
            response = Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorDescriptor.Builder().setMessage("Resource not found")
                            .setErrorCode(ResourceNotFoundException.ERROR_CODE_RESOURCE_NOT_FOUND)
                            .setParameters(e.getArgs()).getErrorDescriptor())
                    .build();
        }catch (org.springframework.security.AccessDeniedException e){
            response = Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }catch (com.jaspersoft.jasperserver.remote.exception.AccessDeniedException e){
            response = Response.status(Response.Status.FORBIDDEN).entity(e.getErrorDescriptor()).build();
        }catch (RemoteException e){
            response = Response.status(Response.Status.BAD_REQUEST).entity(e.getErrorDescriptor()).build();
        }catch (WebApplicationException e){
            response = e.getResponse();
        }catch (Exception e) {
            response = Response.serverError().entity(new ErrorDescriptor(e)).build();
            log.error("Unexpected error occurs", e);
        }
        return (ResponseType)response;
    }
}
