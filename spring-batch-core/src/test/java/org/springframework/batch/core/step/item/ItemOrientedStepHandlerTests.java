/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.step.item;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.NoWorkFoundException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemReader;
import org.springframework.batch.item.support.AbstractItemWriter;
import org.springframework.batch.item.support.PassthroughItemProcessor;

/**
 * @author Dave Syer
 * 
 */
public class ItemOrientedStepHandlerTests {

	private StubItemReader itemReader = new StubItemReader();

	private StubItemWriter itemWriter = new StubItemWriter();

	@Test
	public void testHandle() throws Exception {
		ItemOrientedStepHandler<String, String> handler = new ItemOrientedStepHandler<String, String>(itemReader,
				new PassthroughItemProcessor<String>(), itemWriter);
		StepContribution contribution = new StepContribution(new StepExecution("foo", new JobExecution(new JobInstance(
				123L, new JobParameters(), "job"))));
		handler.handle(contribution);
		assertEquals(1, itemReader.count);
		assertEquals("1", itemWriter.values);
	}

	@Test
	public void testHandleCompositeItem() throws Exception {
		ItemOrientedStepHandler<String, String> handler = new ItemOrientedStepHandler<String, String>(itemReader,
				new AgrgegateItemProcessor(), itemWriter);
		StepContribution contribution = new StepContribution(new StepExecution("foo", new JobExecution(new JobInstance(
				123L, new JobParameters(), "job"))));
		handler.handle(contribution);
		assertEquals(2, itemReader.count);
		assertEquals("12", itemWriter.values);
	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private final class AgrgegateItemProcessor implements ItemProcessor<String, String> {
		private int count = 0;

		private String value = "";

		public String process(String item) throws Exception {
			value += item;
			if (count++ < 1) {
				return null;
			}
			String result = value;
			value = "";
			count = 0;
			return result;
		}
	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private final class StubItemWriter extends AbstractItemWriter<String> {
		private String values = "";

		public void write(String item) throws Exception {
			values += item;
		}
	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private final class StubItemReader extends AbstractItemReader<String> {
		private int count = 0;

		public String read() throws Exception, UnexpectedInputException, NoWorkFoundException, ParseException {
			if (count++ < 5)
				return "" + count;
			return null;
		}
	}

}
