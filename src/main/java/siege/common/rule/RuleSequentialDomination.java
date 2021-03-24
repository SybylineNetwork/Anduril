package siege.common.rule;

public class RuleSequentialDomination extends Rule {
	
	public RuleSequentialDomination() {
	}

	@Override
	public SiegeRule rule() {
		return SiegeRule.SEQUENTIAL_DOMINATION;
	}
	
}