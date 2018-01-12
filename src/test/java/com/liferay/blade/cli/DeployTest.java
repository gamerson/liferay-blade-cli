package com.liferay.blade.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.liferay.blade.cli.ShellCommand.ShellOptions;
import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.gradle.GradleTooling;

import aQute.lib.io.IO;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GradleTooling.class, GradleExec.class, blade.class, bladenofail.class, Util.class, ShellCommand.class})
public class DeployTest {
	private File testDir;

	@Before
	public void setUp() throws Exception {
		testDir = Files.createTempDirectory("samplestest").toFile();
	}

	@After
	public void cleanUp() throws Exception {
		if (testDir.exists()) {
			IO.delete(testDir);
			assertFalse(testDir.exists());
		}
	}
	
	@Test
	public void testDeployWar() throws Exception {
		File war = new File(testDir, "test.war");
		
		String fileNameWithoutExt = war.getName().substring(0,  war.getName().lastIndexOf("."));
		
		assertTrue(war.createNewFile());

		PowerMock.mockStaticNice(Util.class);
		
		EasyMock.expect(Util.canConnect(EasyMock.anyString(), EasyMock.anyInt())).andStubReturn(true);
		
		PowerMock.replay(Util.class);
		
		PowerMock.mockStatic(GradleTooling.class);
		
		EasyMock.expect(GradleTooling.getOutputFiles(EasyMock.isA(File.class), EasyMock.isA(File.class))).andStubReturn(new HashSet<>(Arrays.asList(war)));
		
		PowerMock.replay(GradleTooling.class);
		
		GradleExec gradle = EasyMock.createNiceMock(GradleExec.class);
		
		EasyMock.expect(gradle.executeGradleCommand(EasyMock.anyString())).andStubReturn(0);
				
		EasyMock.replay(gradle);
		
		PowerMock.mockStatic(GradleExec.class);
		
		EasyMock.expect(GradleExec.get(EasyMock.isA(blade.class))).andStubReturn(gradle);

		PowerMock.replay(GradleExec.class);
	
		PowerMock.mockStatic(ShellCommand.class);
		
		EasyMock.expect(ShellCommand.get(EasyMock.isA(blade.class), EasyMock.isA(ShellOptions.class))).andStubAnswer(()->
		{
			final Object[] conArgs = EasyMock.getCurrentArguments();
			
			ShellOptions options = (ShellOptions)conArgs[1];
			
			ShellCommand c = EasyMock.createNiceMock(ShellCommand.class);
			
			c.execute();
			
			EasyMock.expectLastCall().andReturn(null).once();
			
			EasyMock.replay(c);
			
			assertEquals(options._arguments().size(), 2);
			
			assertEquals(options._arguments().get(0), "install");
	
			assertEquals(options._arguments().get(1),  String.format("webbundle:file:%s?Web-ContextPath=/%s", war.getAbsolutePath(), fileNameWithoutExt));
			
			return c;
		});
		
		PowerMock.replay(ShellCommand.class);
		
		String[] args = {
				"-b",testDir.getAbsolutePath(), "deploy"
		};

		blade bl= new bladenofail();
		
		bl.run(args);
		
		PowerMock.verifyAll();
		
	}
}
