/*
 * Copyright (c) 2013, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner.event;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.TestConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static org.aeonbits.owner.UtilTest.save;
import static org.aeonbits.owner.event.PropertyChangeMatcher.matches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author Luigi R. Viggiano
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyChangeListenerOnReloadTest implements TestConstants {
    private static final String spec = "file:" + RESOURCES_DIR + "/PropertyChangeListenerOnReloadTest.properties";
    private File target;
    @Mock
    private TransactionalPropertyChangeListener listener;
    private MyConfig cfg;

    @Before
    public void before() throws MalformedURLException {
        target = new File(new URL(spec).getFile());
        target.delete();
        cfg = ConfigFactory.create(MyConfig.class);
        cfg.addPropertyChangeListener(listener);
    }

    @After
    public void after() {
        target.delete();
    }

    @Sources(spec)
    interface MyConfig extends Mutable {
        @DefaultValue("5")
        Integer someInteger();

        @DefaultValue("foobar")
        String someString();

        @DefaultValue("3.14")
        Double someDouble();

        String nullsByDefault();
    }

    @Test
    public void testReloadOnRollbackBatchException() throws Throwable {

        save(target, new Properties() {{
            setProperty("someInteger", "5");
            setProperty("someString", "bazbar");
            setProperty("someDouble", "2.718");
            setProperty("nullByDefault", "NotNullNow");
        }});

        doNothing().doNothing().doThrow(new RollbackBatchException())
                .when(listener).beforePropertyChange(any(PropertyChangeEvent.class));

        cfg.reload();

        assertEquals(new Integer(5), cfg.someInteger());
        assertEquals("foobar", cfg.someString());
        assertEquals(new Double("3.14"), cfg.someDouble());
        assertNull(cfg.nullsByDefault());
    }


    @Test
    public void testReloadOnRollbackOperationException() throws Throwable {

        save(target, new Properties() {{
            setProperty("someString", "bazbar");
            setProperty("someDouble", "2.718");
            setProperty("nullsByDefault", "NotNullNow");
        }});

        PropertyChangeEvent eventToRollback = new PropertyChangeEvent(cfg, "someString", "foobar", "bazbar");

        doThrow(new RollbackOperationException())
                .when(listener).beforePropertyChange(argThat(matches(eventToRollback)));

        cfg.reload();

        assertEquals(new Integer(5), cfg.someInteger());
        assertEquals("foobar", cfg.someString());
        assertEquals(new Double("2.718"), cfg.someDouble());
        assertEquals("NotNullNow", cfg.nullsByDefault());
    }

    @Test
    public void testReloadWhenNoChangesHaveBeenMade() throws Throwable {
        cfg.reload();
        verifyZeroInteractions(listener);
    }

    @Test
    public void testReloadWhenChangeHappen() throws Throwable{
        save(target, new Properties() {{
            setProperty("someInteger", "5");
            setProperty("someString", "bazbar");
            setProperty("someDouble", "2.718");
            setProperty("nullByDefault", "NotNullNow");
        }});

        cfg.reload();

        verify(listener, times(3)).beforePropertyChange(any(PropertyChangeEvent.class));
        verify(listener, times(3)).propertyChange(any(PropertyChangeEvent.class));

        PropertyChangeEvent someStringChange = new PropertyChangeEvent(cfg, "someString", "foobar", "bazbar");
        PropertyChangeEvent someDoubleChange = new PropertyChangeEvent(cfg, "someDouble", "3.14", "2.718");
        PropertyChangeEvent nullByDefaultChange = new PropertyChangeEvent(cfg, "nullByDefault", null, "NotNullNow");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(someStringChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(someStringChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(someDoubleChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(someDoubleChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(nullByDefaultChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(nullByDefaultChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(someStringChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(someDoubleChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(someDoubleChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(someStringChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(someStringChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(nullByDefaultChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(someDoubleChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(nullByDefaultChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(nullByDefaultChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(someStringChange)));

        inOrder = inOrder(listener);
        inOrder.verify(listener, times(1)).beforePropertyChange(argThat(matches(nullByDefaultChange)));
        inOrder.verify(listener, times(1)).propertyChange(argThat(matches(someDoubleChange)));

        verifyNoMoreInteractions(listener);
    }

}
