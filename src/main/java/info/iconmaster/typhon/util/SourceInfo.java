package info.iconmaster.typhon.util;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

/**
 * This represents an area in Typhon source code.
 * Used for debug and error reporting purposes.
 * 
 * @author iconmaster
 *
 */
public class SourceInfo {
	/**
	 * The string that will be used if the source file is not provided.
	 */
	public static final String UNKNOWN_FILE = "<unknown>";
	
	/**
	 * The file this originates from.
	 */
	public String file;
	
	/**
	 * The 0-indexed, inclusive character location of the beginning of the region this originates from.
	 */
	public int begin;
	
	/**
	 * The 0-indexed, inclusive character location of the end of the region this originates from.
	 */
	public int end;
	
	/**
	 * Directly constructs a SourceInfo.
	 * 
	 * @param file The file this originates from.
	 * @param begin The 0-indexed, inclusive character location of the beginning of the region this originates from.
	 * @param end The 0-indexed, inclusive character location of the end of the region this originates from.
	 */
	public SourceInfo(String file, int begin, int end) {
		this.file = file;
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	/**
	 * Directly constructs a SourceInfo.
	 * 
	 * @param begin The 0-indexed, inclusive character location of the beginning of the region this originates from.
	 * @param end The 0-indexed, inclusive character location of the end of the region this originates from.
	 */
	public SourceInfo(int begin, int end) {
		this(UNKNOWN_FILE, begin, end);
	}
	
	/**
	 * Constructs a SourceInfo from an ANTLR token.
	 * 
	 * @param token The token this originates from.
	 */
	public SourceInfo(Token token) {
		this.file = token.getTokenSource().getSourceName();
		int begin = token.getStartIndex();
		int end = token.getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	/**
	 * Constructs a SourceInfo from a range of ANTLR tokens.
	 * 
	 * @param beginToken The first token this originates from.
	 * @param endToken The last token this originates from.
	 */
	public SourceInfo(Token beginToken, Token endToken) {
		this.file = beginToken.getTokenSource().getSourceName();
		int begin = beginToken.getStartIndex();
		int end = endToken.getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	/**
	 * Constructs a SourceInfo from an ANTLR rule.
	 * 
	 * @param rule The rule this originates from.
	 */
	public SourceInfo(ParserRuleContext rule) {
		this.file = rule.getStart() == null? UNKNOWN_FILE : rule.getStart().getTokenSource().getSourceName();
		int begin = rule.getStart() == null? 0 : rule.getStart().getStartIndex();
		int end = rule.getStop() == null? 0 : rule.getStop().getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	/**
	 * Constructs a SourceInfo from a range of ANTLR rules.
	 * 
	 * @param beginRule The first rule this originates from.
	 * @param endRule The last rule this originates from.
	 */
	public SourceInfo(ParserRuleContext beginRule, ParserRuleContext endRule) {
		this.file = beginRule.getStart() == null? UNKNOWN_FILE : beginRule.getStart().getTokenSource().getSourceName();
		int begin = beginRule.getStart() == null? 0 : beginRule.getStart().getStartIndex();
		int end = endRule.getStart() == null? 0 : endRule.getStop().getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	/**
	 * Constructs a SourceInfo from a range of ANTLR rules.
	 * 
	 * @param rules The set of rules this originates from.
	 */
	public SourceInfo(List<? extends ParserRuleContext> rules) {
		if (rules.isEmpty()) return;
		
		this.file = rules.get(0).getStart() == null? UNKNOWN_FILE : rules.get(0).getStart().getTokenSource().getSourceName();
		int begin = rules.get(0).getStart() == null? 0 : rules.get(0).getStart().getStartIndex();
		int end = rules.get(rules.size()-1).getStop() == null? 0 : rules.get(rules.size()-1).getStop().getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	@Override
	public String toString() {
		return file+": "+begin+"-"+end;
	}
}
