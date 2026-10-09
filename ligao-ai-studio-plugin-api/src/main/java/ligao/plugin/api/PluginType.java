package ligao.plugin.api;

/**
 * Plugin types covering the main extension scenarios.
 *
 * @author Zhouyin.Wei
 */
public enum PluginType {

    /** Register new agent tools */
    TOOL,

    /** Register new LLM providers */
    PROVIDER,

    /** Register new messaging channels */
    CHANNEL,

    /** Register new memory providers */
    MEMORY
}
