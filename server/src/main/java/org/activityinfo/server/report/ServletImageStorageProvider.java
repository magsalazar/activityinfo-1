/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.activityinfo.server.report;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.activityinfo.server.authentication.SecureTokenGenerator;
import org.activityinfo.server.report.renderer.html.ImageStorage;
import org.activityinfo.server.report.renderer.html.ImageStorageProvider;

import com.google.inject.Provider;

public class ServletImageStorageProvider implements ImageStorageProvider {

    private String urlBase;
    private String tempPath;
	private Provider<HttpServletRequest> provider;

	public ServletImageStorageProvider(String urlBase, String tempPath, Provider<HttpServletRequest> httpRequestProvider) {
        this.urlBase = urlBase;
        this.tempPath = tempPath;
        this.provider = httpRequestProvider;
    }

    @Override
    public ImageStorage getImageUrl(String suffix) throws IOException {
        String filename = SecureTokenGenerator.generate() + suffix;
        HttpServletRequest req = provider.get();
        
        StringBuilder sb = new StringBuilder()
	        .append("http://")
	        .append(req.getServerName())
	        .append(":")
	        .append(req.getServerPort())
	        .append("/")
	        .append(urlBase)
	        .append(filename);

        String path = tempPath + "/" + filename;

        FileOutputStream os = new FileOutputStream(path);

        return new ImageStorage(sb.toString(), os);
    }
}
