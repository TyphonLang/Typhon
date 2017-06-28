package info.iconmaster.typhon.util;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class SourceInfo {
	public String file;
	public int begin,end;
	
	public SourceInfo(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
	public SourceInfo(String file, int begin, int end) {
		this.file = file;
		this.begin = begin;
		this.end = end;
	}
	
	public SourceInfo(Token token) {
		this.file = token.getTokenSource().getSourceName();
		this.begin = token.getStartIndex();
		this.end = token.getStopIndex();
	}
	
	public SourceInfo(Token begin, Token end) {
		this.file = begin.getTokenSource().getSourceName();
		this.begin = begin.getStartIndex();
		this.end = end.getStopIndex();
	}
	
	public SourceInfo(ParserRuleContext rule) {
		this.file = rule.getStart().getTokenSource().getSourceName();
		this.begin = rule.getStart().getStartIndex();
		this.end = rule.getStop().getStopIndex();
		
		if (this.begin == -1) this.begin = this.end;
		if (this.end == -1) this.end = this.begin;
	}
	
	public SourceInfo(ParserRuleContext begin, ParserRuleContext end) {
		this.file = begin.getStart().getTokenSource().getSourceName();
		this.begin = begin.getStart().getStartIndex();
		this.end = end.getStop().getStopIndex();
		
		if (this.begin == -1) this.begin = this.end;
		if (this.end == -1) this.end = this.begin;
	}
	
	public SourceInfo(List<? extends ParserRuleContext> rules) {
		if (rules.isEmpty()) return;
		
		this.file = rules.get(0).getStart().getTokenSource().getSourceName();
		this.begin = rules.get(0).getStart().getStartIndex();
		this.end = rules.get(rules.size()-1).getStop().getStopIndex();
		
		if (this.begin == -1) this.begin = this.end;
		if (this.end == -1) this.end = this.begin;
	}
	
	@Override
	public String toString() {
		return (file == null?"<unknown>":file)+": "+begin+"-"+end;
	}
}
