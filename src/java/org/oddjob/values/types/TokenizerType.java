package org.oddjob.values.types;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.utils.ArooaTokenizer;
import org.oddjob.arooa.utils.FlexibleTokenizerFactory;

/**
 * @oddjob.description Tokenizes text. This type provides conversion to an array
 * or list of strings.
 * <p>
 * The delimiter can be provided as either plain text or a regular expression.
 * The default delimiter is the regular expression <code>\s*,\s*</code> which is
 * CSV with optional white space either side.
 *  
 * @oddjob.example Tokenize comma separated values.
 * 
 * {@oddjob.xml.resource org/oddjob/values/types/TokenizeExample.xml}
 * 
 * @author rob
 *
 */
public class TokenizerType implements ArooaValue {
	
	public static final ArooaElement ELEMENT = new ArooaElement("tokenize");
	
	public static final String DEFAULT_DELIMITER_REGEXP = "\\s*,\\s*";
			
	public static class Conversions implements ConversionProvider {
		
		@SuppressWarnings("rawtypes")
		public void registerWith(ConversionRegistry registry) {
			registry.register(TokenizerType.class, List.class, 
					new Convertlet<TokenizerType, List>() {
				@Override
				public List convert(TokenizerType from)
						throws ConvertletException {
					try {
						return Arrays.asList(from.parse());
					} catch (ParseException e) {
						throw new ConvertletException(e);
					}
				}
			});
			registry.register(TokenizerType.class, String[].class, 
					new Convertlet<TokenizerType, String[]>() {
				@Override
				public String[] convert(TokenizerType from)
						throws ConvertletException {
					try {
						return from.parse();
					} catch (ParseException e) {
						throw new ConvertletException(e);
					}
				}
			});
		}
	}
	
	/**
	 * @oddjob.property
	 * @oddjob.description The delimiter as a string.
	 * @oddjob.required No.
	 */
	private String delimiter;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The delimiter as a regular expression.
	 * @oddjob.required No.
	 */
	private String regexp;
	
	/**
	 * @oddjob.property
	 * @oddjob.description An escape character to use.
	 * @oddjob.required No.
	 */
	private Character escape;
	
	/**
	 * @oddjob.property
	 * @oddjob.description An quote character to use.
	 * @oddjob.required No.
	 */
	private Character quote;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The value to parse.
	 * @oddjob.required No. If missing the result of the conversion will be
	 * null.
	 */
	private String text;
	
	public String[] parse() throws ParseException {
		
		if (text == null) {
			return null; 
		}

		String regexp = this.regexp;
		if (regexp == null && delimiter == null) {
			regexp = DEFAULT_DELIMITER_REGEXP;
		}
		
		FlexibleTokenizerFactory tokenizerFactory = new FlexibleTokenizerFactory();
		tokenizerFactory.setDelimiter(delimiter);
		tokenizerFactory.setRegexp(regexp);
		tokenizerFactory.setEscape(escape);
		tokenizerFactory.setQuote(quote);
		
		ArooaTokenizer tokenizer = tokenizerFactory.newTokenizer();
		
		return tokenizer.parse(text);
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public Character getEscape() {
		return escape;
	}

	public void setEscape(Character escape) {
		this.escape = escape;
	}

	public Character getQuote() {
		return quote;
	}

	public void setQuote(Character quote) {
		this.quote = quote;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
