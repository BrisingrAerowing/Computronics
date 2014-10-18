package pl.asie.computronics.util;

import li.cil.oc.api.driver.EnvironmentHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import pl.asie.lib.util.ChatUtils;

public class ChatBoxUtils {
	public static void sendChatMessage(World worldObj, int xCoord, int yCoord, int zCoord, int distance, String prefix, String string) {
		String text = EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + "["+prefix+"] ";
		text += EnumChatFormatting.RESET + "" + EnumChatFormatting.GRAY + ChatUtils.color(string);
		for(Object o: worldObj.playerEntities) {
			if(!(o instanceof EntityPlayer)) continue;
			EntityPlayer player = (EntityPlayer)o;
			if(player.getDistance(xCoord, yCoord, zCoord) < distance) {
				player.addChatMessage(new ChatComponentText(text));
			}
		}
	}
	
	public static void sendChatMessage(TileEntity te, int d, String prefix, String string) {
		if(te == null) return;
		sendChatMessage(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, d, prefix, string);
	}

	public static void sendChatMessage(EnvironmentHost c, int d, String prefix, String string) {
		if(c == null) return;
		sendChatMessage(c.world(), (int)Math.round(c.xPosition()), (int)Math.round(c.yPosition()), (int)Math.round(c.zPosition()), d, prefix, string);
	}
}
