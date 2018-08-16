package core.extension;

import com.liferay.portal.deploy.hot.CustomJspBag;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.url.URLContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.net.URL;
import java.util.*;

@Component(
        immediate = true,
        property = {
                "context.id=UpdateSecurityQuestionsJspBag", "context.name=Update Security Questions JSP Bag",
                "service.ranking:Integer=100"
        }
)
public class UpdateSecurityQuestionsJspBag implements CustomJspBag {

    private Bundle _bundle;
    private List<String> _customJsps;

    private final URLContainer _urlContainer = new URLContainer() {

        @Override
        public URL getResource(String name) {
            return _bundle.getEntry(name);
        }

        @Override
        public Set<String> getResources(String path) {
            Set<String> paths = new HashSet<>();

            for (String entry : _customJsps) {
                if (entry.startsWith(path)) {
                    paths.add(entry);
                }
            }

            return paths;
        }

    };

    @Override
    public String getCustomJspDir() {
        return "META-INF/jsps/";
    }

    @Override
    public List<String> getCustomJsps() {
        return _customJsps;
    }

    @Override
    public URLContainer getURLContainer() {
        return _urlContainer;
    }

    @Override
    public boolean isCustomJspGlobal() {
        return true;
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        _bundle = bundleContext.getBundle();

        _customJsps = new ArrayList<>();

        Enumeration<URL> jspEntries = _bundle.findEntries(
                getCustomJspDir(), "*.jsp", true);

        while (jspEntries.hasMoreElements()) {
            URL url = jspEntries.nextElement();

            _customJsps.add(url.getPath());
        }

        Enumeration<URL> jspfEntries = _bundle.findEntries(
                getCustomJspDir(), "*.jspf", true);

        while (jspfEntries.hasMoreElements()) {
            URL url = jspfEntries.nextElement();

            _customJsps.add(url.getPath());
        }

    }
}
