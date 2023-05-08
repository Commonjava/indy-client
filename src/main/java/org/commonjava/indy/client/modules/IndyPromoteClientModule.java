/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.client.modules;

import org.apache.http.HttpStatus;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.util.UrlUtils;
import org.commonjava.indy.client.model.StoreKey;
import org.commonjava.indy.client.model.promote.PathsPromoteRequest;
import org.commonjava.indy.client.model.promote.PathsPromoteResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IndyPromoteClientModule
    extends IndyClientModule
{

    public static final String PROMOTE_BASEPATH = "promotion";

    public static final String PATHS_PROMOTE_PATH = PROMOTE_BASEPATH + "/paths/promote";

    public static final String PATHS_ROLLBACK_PATH = PROMOTE_BASEPATH + "/paths/rollback";

    public PathsPromoteResult promoteByPath( String trackingId, final StoreKey src, final StoreKey target, final boolean purgeSource,
                                             final String... paths )
            throws IndyClientException
    {
        final PathsPromoteRequest req =
                new PathsPromoteRequest( src, target, new HashSet( Arrays.asList( paths ) ) )
                        .setTrackingId( trackingId ).setPurgeSource( purgeSource );

        final PathsPromoteResult
                result = http.postWithResponse( PATHS_PROMOTE_PATH, req, PathsPromoteResult.class, HttpStatus.SC_OK );
        return result;
    }

    public PathsPromoteResult promoteByPath( final PathsPromoteRequest req )
        throws IndyClientException
    {
        final PathsPromoteResult
                result = http.postWithResponse( PATHS_PROMOTE_PATH, req, PathsPromoteResult.class, HttpStatus.SC_OK );
        return result;
    }

    public Set<String> getPromotablePaths( final StoreKey storeKey )
        throws IndyClientException
    {
        final PathsPromoteResult result = promoteByPath( new PathsPromoteRequest( storeKey, storeKey ).setDryRun( true ) );
        return result.getPendingPaths();
    }

    public PathsPromoteResult rollbackPathPromote( final PathsPromoteResult result )
        throws IndyClientException
    {
        return http.postWithResponse( PATHS_ROLLBACK_PATH, result, PathsPromoteResult.class, HttpStatus.SC_OK );
    }

    public String promoteUrl()
    {
        return UrlUtils.buildUrl( http.getBaseUrl(), PATHS_PROMOTE_PATH );
    }

    public String rollbackUrl()
    {
        return UrlUtils.buildUrl( http.getBaseUrl(), PATHS_ROLLBACK_PATH );
    }

}
