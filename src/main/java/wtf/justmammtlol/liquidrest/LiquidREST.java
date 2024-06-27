package wtf.justmammtlol.liquidrest;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;


import java.io.IOException;

@Mod(LiquidREST.MOD_ID)
public class LiquidREST {
    RestServer rest = new RestServer();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "liquidrest";

    public final MinecraftServer server =  ServerLifecycleHooks.getCurrentServer();


    public LiquidREST() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, LiquidRESTServerConfigs.SPEC, "liquidrest-server.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void setup(final FMLCommonSetupEvent event) {

    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) throws IOException {
        // Do something when the server starts
        LOGGER.info("Starting LiquidREST threads");
        rest.main();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) throws IOException {
        // Do something when the server starts
        LOGGER.info("Shutting down LiquidREST threads");
        rest.stop();
    }

}
