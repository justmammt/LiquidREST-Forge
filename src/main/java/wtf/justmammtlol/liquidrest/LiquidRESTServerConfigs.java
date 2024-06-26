package wtf.justmammtlol.liquidrest;

import net.minecraftforge.common.ForgeConfigSpec;

public class LiquidRESTServerConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> WEBSERVER_PORT;

    public static final ForgeConfigSpec.ConfigValue<Integer> WEBSERVER_FIXED_THREADS_COUNT;

    public static final ForgeConfigSpec.ConfigValue<String> WEBSERVER_BASICAUTH_USER;

    public static final ForgeConfigSpec.ConfigValue<String> WEBSERVER_BASICAUTH_PASS;

    static {
        BUILDER.push("Configuration for LiquidREST");

        WEBSERVER_PORT = BUILDER.comment("The port to run the REST API on:")
                .define("Port", 8010);

        WEBSERVER_FIXED_THREADS_COUNT = BUILDER.comment("The amount of threads the http server should rely on:")
                .defineInRange("Threads", 4, 1, 2048);

        BUILDER.push("Webserver Authentication Configuration");

        WEBSERVER_BASICAUTH_USER = BUILDER.comment("The Authentication username")
                .define("Username", "user");

        WEBSERVER_BASICAUTH_PASS = BUILDER.comment("The Authentication password")
                .define("Password", "pass");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
