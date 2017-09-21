package info.iconmaster.typhon.util;

public class StringUtils {
	private StringUtils() {}
	
	public static String formatTyphonString(String s) {
		s = s.substring(1, s.length()-1);
		StringBuilder sb = new StringBuilder();
		
		while (!s.isEmpty()) {
			char c = s.charAt(0);
			s = s.substring(1);
			
			if (c == '\\') {
				if (s.isEmpty()) {
					return null;
				}
				
				c = s.charAt(0);
				s = s.substring(1);
				
				switch (c) {
				case 'b':
					sb.append('\b'); break;
				case 't':
					sb.append('\t'); break;
				case 'n':
					sb.append('\n'); break;
				case 'f':
					sb.append('\f'); break;
				case 'r':
					sb.append('\r'); break;
				case '\'':
					sb.append('\''); break;
				case '\"':
					sb.append('\"'); break;
				case '\\':
					sb.append('\\'); break;
				default:
					return null;
				}
			} else {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
}
