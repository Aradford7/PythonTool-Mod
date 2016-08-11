package pycraft.scriptitem;

import java.io.IOException;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * ----------- PyCraft Mod -----------
 * Alvaro Perez & Hans Fangohr
 * University of Southampton, UK (2016)
 *
 * Code based on MinecraftByExample by TheGreyGhost: MBE11_ITEM_VARIANTS
 * https://github.com/TheGreyGhost/MinecraftByExample/tree/1-8final
 * (mbe11_item_variants/ItemVariants.java)
 * 
 * Original code by:
 * User: The Grey Ghost
 * Date: 24/12/2014
 * 
 * Create an ScriptItem, which represents a python script. It can be carried and used as any
 * other tool. When equipped and used it will run the associated python script, and
 * consume one unit from the item stack. If only right clicked, it will run the python script
 * and stop any previously used scripts running in the background. If sneak+right clicked, it will
 * run the new script and keep any already running ones. ScriptItem uses the custom commands
 * /python and /apython from Forge's RaspberryJam mod to run the python scripts, so RaspberryJam
 * mod is needed.
 * 
 * The associated python script can be arbitrary, although it is intended to use the mcpipy library to
 * interface with the game in real time: http://www.stuffaboutcode.com/p/minecraft.html
 * 
 * There are subvariants to this item, with the same functionality, but changing the item texture:
 * the intention is to roughly categorize the script behaviour and offer different ScriptItem textures
 * to be able to visually remember what the script does in the case that we have a high number of them.
 * For example, if a given script builds a house, it can have a house icon in its texture. If it
 * teleports the player, it can have a teleport icon.
 * 
 * Currently, this is achieved by adding the code # metadata NUMBER as the first line in the python script:
 *      NUMBER = 0 or no metadata at all: normal script
 *      NUMBER = 1: housing script
 *      ...
 */

public class ScriptItem extends Item {
	public ScriptItem() {
		super();
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setUnlocalizedName("scriptItem");
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	// add a subitem for each item we want to appear in the creative tab
	//  in this case - a full bottle of each colour
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List subItems)
	{
		ItemStack subItemStack1 = new ItemStack(itemIn, 1, 1);
		subItems.add(subItemStack1);
		ItemStack subItemStack2 = new ItemStack(itemIn, 1, 2);
		subItems.add(subItemStack2);
	}

	@Override
	// Make a unique name for each contents type (lime, orange, etc) so we can name them individually
	//  The fullness information is added separately in getItemStackDisplayName()
	public String getUnlocalizedName(ItemStack stack)
	{
		int metadata = stack.getMetadata();
		return super.getUnlocalizedName() + "." + metadata;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		// Get the script path from the NBT data
		NBTTagCompound nbtTagCompound = stack.getTagCompound();
		if (nbtTagCompound == null) {
			if (world.isRemote) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
						"NO ASSOCIATED SCRIPT"));
			}
			// Decrease stack size
			stack.stackSize--;
			return stack;
		}

		if (world.isRemote) {
			if (player.isSneaking()) { // shift pressed. Run new parallel script
				String scriptName = nbtTagCompound.getString("scriptName");
				MinecraftServer.getServer().getCommandManager().executeCommand(player,
						"/apy " + scriptName);
			} else { // shift not pressed. Cancel previous scripts and run new script
			String scriptName = nbtTagCompound.getString("scriptName");
			MinecraftServer.getServer().getCommandManager().executeCommand(player,
					"/python " + scriptName);
			}
		}

		// Decrease stack size
		if (!player.capabilities.isCreativeMode) {
			stack.stackSize--;
		}
		return stack;
	}

	// adds 'tooltip' text
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
		NBTTagCompound nbtTagCompound = stack.getTagCompound();
		if (nbtTagCompound != null && nbtTagCompound.hasKey("scriptName")) {
			tooltip.add("Loaded script: " + nbtTagCompound.getString("scriptName"));
		}
		else
		{
			tooltip.add("NO SCRIPT LOADED");
		}
	}

	// change the displayed stack name depending on the fullness
	// the contents are already incorporated into the unlocalizedName
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String s = ("" + StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name")).trim();
		int metadata = stack.getMetadata();
		return s;
	}

}