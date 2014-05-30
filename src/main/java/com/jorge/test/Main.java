package com.jorge.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

public class Main {
    
    private static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws Exception {
        CommandLineOptions cmd = new CommandLineOptions();
        final JCommander commander = new JCommander(cmd, args);
        
        if (cmd.isHelp()) {
            commander.usage();
            return;
        }
        
        if (cmd.getConfigurationFile() != null) {
        	final Properties properties = new Properties();
        	final InputStream inputStream = new FileInputStream(cmd.getConfigurationFile());
        	try {
        		properties.load(inputStream);
        	} finally {
        		try {
					inputStream.close();
				} catch (IOException ignored) { }
        	}
        	ApplicationProperties.properties.putAll(properties);
        }
        
        if (!((cmd.getQueueJndiName() == null) || (cmd.getQueueJndiName().length() == 0))) {
        	ApplicationProperties.properties.put("test.queue.jndiName", cmd.getQueueJndiName());
        }
        
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        try {
            JmsTemplate jmsTemplate = context.getBean("jmsTemplate", JmsTemplate.class);
            
            Destination testQueue = context.getBean("CoreQueue", Destination.class);
            
            if (log.isInfoEnabled()) {
                log.info(testQueue);
            }
            
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                StringBuilder data = new StringBuilder(256);
                int state = 0;
                int paren = 0;
                String packet = null;
                for (String line = null; (line = reader.readLine()) != null; ) {
                    for (int i = 0, count = line.length(); i < count; ++i) {
                        final char ch = line.charAt(i);
                        data.append(ch);
                        if (ch == '{') {
                            ++paren;
                            state = 1;
                        } else if (ch == '}') {
                            if (state == 1) {
                                if (--paren == 0) {
                                    state = 2;
                                    packet = data.toString();
                                    data = new StringBuilder(256);
                                    if (i + 1 < count) {
                                        data.append(line.substring(i + 1));
                                    }
                                }
                            }
                        }
                    }
                    
                    if (state == 2) {
                        assert (packet != null);
                        assert (packet.endsWith("}"));
                        
                        final int start = packet.indexOf("{");
                        
                        assert (start >= 0);
                        
                        final String properties = (start == 0 ? null : packet.substring(0, start));
                        final String text = packet.substring(start + 1, packet.length() - 1);
                        
                        final Properties p = new Properties();
                        if (properties != null) {
                            final StringReader r = new StringReader(properties);
                            try {
                                p.load(r);
                            } finally {
                                r.close();
                            }
                        }
                        
                        jmsTemplate.send(testQueue, new MessageCreator() {
                            
                            public Message createMessage(Session session) throws JMSException {
                                final Message message = session.createTextMessage(text);
                                for (Object o : p.keySet()) {
                                    String s = (String) o;
                                    if (s.startsWith("JMS")) {
                                        if (s.equals("JMSCorrelationID")) {
                                            message.setJMSCorrelationID(p.getProperty(s));
                                        }
                                    } else {
                                        message.setStringProperty(s, p.getProperty(s));
                                    }
                                }
                                return message;
                            }
                        });
                        
                        packet = null;
                        state = 0;
                    } else {
                        data.append('\n');
                    }                        
                }
            } finally {
                reader.close();
            }
        } finally {
            context.close();
        }
    }
    
    @Parameters(separators = "= ")
    static class CommandLineOptions {
        
        @Parameter(names = { "-q", "--queue" }, description = "Queue JNDI name")
        private String queueJndiName;
        
        @Parameter(names = { "-h", "--help" }, help = true)
        private boolean help;
        
        @Parameter(names = { "-c", "--configuration" }, description = "Configuration file")
        private String configurationFile;
        
        public String getQueueJndiName() {
            return queueJndiName;
        }

        public void setQueueJndiName(String queueJndiName) {
            this.queueJndiName = queueJndiName;
        }
        
        public String getConfigurationFile() {
			return configurationFile;
		}

		public void setConfigurationFile(String configurationFile) {
			this.configurationFile = configurationFile;
		}

        public boolean isHelp() {
            return help;
        }

        public void setHelp(boolean help) {
            this.help = help;
        }
        
    }

}
