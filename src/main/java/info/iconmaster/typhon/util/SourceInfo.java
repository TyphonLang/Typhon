package info.iconmaster.typhon.util;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class SourceInfo {
	public static final String UNKNOWN_FILE = "<unknown>";
	
	public String file;
	public int begin,end;
	
	public SourceInfo(String file, int begin, int end) {
		this.file = file;
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	public SourceInfo(int begin, int end) {
		this(UNKNOWN_FILE, begin, end);
	}
	
	public SourceInfo(Token token) {
		this.file = token.getTokenSource().getSourceName();
		int begin = token.getStartIndex();
		int end = token.getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	public SourceInfo(Token beginToken, Token endToken) {
		this.file = beginToken.getTokenSource().getSourceName();
		int begin = beginToken.getStartIndex();
		int end = endToken.getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	public SourceInfo(ParserRuleContext rule) {
		this.file = rule.getStart() == null? UNKNOWN_FILE : rule.getStart().getTokenSource().getSourceName();
		int begin = rule.getStart() == null? 0 : rule.getStart().getStartIndex();
		int end = rule.getStop() == null? 0 : rule.getStop().getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
	public SourceInfo(ParserRuleContext beginRule, ParserRuleContext endRule) {
		this.file = beginRule.getStart() == null? UNKNOWN_FILE : beginRule.getStart().getTokenSource().getSourceName();
		int begin = beginRule.getStart() == null? 0 : beginRule.getStart().getStartIndex();
		int end = endRule.getStart() == null? 0 : endRule.getStop().getStopIndex();
		
		this.begin = Math.min(begin, end);
		this.end = Math.max(begin, end);
	}
	
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
