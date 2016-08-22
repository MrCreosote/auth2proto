package us.kbase.auth2.service.mustache;

import java.io.StringWriter;
import java.nio.file.Path;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;

/* It's completely stupid that I have to create this class, but there doesn't
 * seem to be any way to get my hands on the jersey mustache processor once
 * it's registered.
 */
public class MustacheProcessor {

	//TODO TEST unit tests
	//TODO JAVADOC
	//TODO NOW make TemplateProcessor interface and use that instead
	
	private static final String SUFFIX = ".mustache";
	
	private final MustacheFactory mf = new DefaultMustacheFactory();
	private final Path templates;
	
	public MustacheProcessor(final Path templateDir) {
		if (templateDir == null) {
			throw new NullPointerException("templateDir");
		}
		templates = templateDir;
	}
	
	// this is only suitable for small objects and templates
	public String process(final String template, final Object model) {
		final Path t = templates.resolve(template);
		final StringWriter sw = new StringWriter();
		mf.compile(t.toString() + SUFFIX).execute(sw, model);
		return sw.toString();
	}
	
}
