package filters

import javax.inject.Inject

import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

final class ApplicationFilters @Inject()(gzip: GzipFilter,
                                         cors: CORSFilter,
                                         csrf: CSRFFilter,
                                         security: SecurityHeadersFilter)
    extends DefaultHttpFilters(gzip, cors, csrf, security)
