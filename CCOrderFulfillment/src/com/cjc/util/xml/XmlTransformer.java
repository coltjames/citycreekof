/**
 * <a href="http://www.code42.com">(c)Code 42 Software, Inc.</a>
 * $Id: XmlTransformer.java,v 1.3 2008/01/10 03:50:02 bbispala Exp $
 */
package com.cjc.util.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The xml transformer declaration. This is used if the default "getter/setter" xml transformer must be overridden by a
 * custom transformer. The Class must implement {@link IXmlTransformer}.
 * 
 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
 * @see IXmlTransformer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlTransformer {

	Class<? extends IXmlTransformer> value();
}
