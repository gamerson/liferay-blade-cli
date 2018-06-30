/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.blade.cli.util;

import java.io.InputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * @author Christopher Bryan Boyd
 */
public class PromptUtil<T> implements Supplier<T> {

	public static boolean askBoolean(String question) {
		return askBoolean(question, System.in, System.out, Optional.empty());
	}

	public static boolean askBoolean(String question, InputStream in, PrintStream out) {
		return askBoolean(question, in, out, Optional.empty());
	}

	public static boolean askBoolean(String question, InputStream in, PrintStream out, Optional<String> defaultAnswer) {
		PromptBuilder<Boolean> builder = new PromptBuilder<>(question);

		builder.withIn(in);
		builder.withOut(out);
		builder.addAnswer("y", true);
		builder.addHiddenAnswer("yes", true);
		builder.addAnswer("n", false);
		builder.addHiddenAnswer("no", false);
		builder.setDefaultAnswer(defaultAnswer);

		PromptUtil<Boolean> prompt = builder.build();

		Optional<Boolean> answer = Optional.ofNullable(prompt.get());

		if (answer.isPresent()) {
			return answer.get();
		}
		else {
			throw new NoSuchElementException("Unable to acquire an answer");
		}
	}

	public static boolean askBoolean(String question, InputStream in, PrintStream out, String defaultAnswer) {
		return askBoolean(question, in, out, Optional.of(defaultAnswer));
	}

	public static boolean askBoolean(String question, String defaultAnswer) {
		return askBoolean(question, System.in, System.out, Optional.of(defaultAnswer));
	}

	public static <T> PromptBuilder<T> builder(String question) {
		return new PromptBuilder<>(question);
	}

	@Override
	public T get() {
		Optional<T> answer = Optional.empty();

		String questionWithPrompt = _question + " " + _buildPromptForQuestion(_defaultAnswer);

		while (!answer.isPresent()) {
			_out.println(questionWithPrompt);

			try (Scanner scanner = new Scanner(_in)) {
				String decision = scanner.nextLine();

				if (!_caseSensitive) {
					decision = decision.toLowerCase();
				}

				decision = decision.trim();

				answer = _findAnswer(decision);

				if (!answer.isPresent()) {
					if (_defaultAnswer.isPresent()) {
						answer = _findAnswer(_getDefaultAnswerString());
					}
					else {
						_out.println("Unrecognized input: " + decision);
						continue;
					}
				}
			}
			catch (NoSuchElementException nsee) {
				if (_defaultAnswer.isPresent()) {
					answer = _findAnswer(_getDefaultAnswerString());
				}
				else {
					_out.println(nsee.getMessage());
					continue;
				}
			}
		}

		return answer.orElse(null);
	}

	public static class PromptBuilder<T> {

		public PromptBuilder(String question) {
			_question = question;
		}

		public PromptBuilder<T> addAnswer(String answer) {
			try {
				return addAnswer(answer, (T)answer);
			}
			catch (ClassCastException cce) {
				throw new UnsupportedOperationException(
					"PromptBuilder<T> T must be of type \"String\" for this functionality", cce);
			}
		}

		public PromptBuilder<T> addAnswer(String answer, T result) {
			_answerMap.put(answer, result);

			return this;
		}

		public PromptBuilder<T> addHiddenAnswer(String answer) {
			try {
				return addHiddenAnswer(answer, (T)answer);
			}
			catch (ClassCastException cce) {
				throw new UnsupportedOperationException(
					"PromptBuilder<T> T must be of type \"String\" for this functionality", cce);
			}
		}

		public PromptBuilder<T> addHiddenAnswer(String answer, T result) {
			_hiddenAnswerMap.put(answer, result);

			return this;
		}

		public PromptUtil<T> build() {
			return new PromptUtil<>(_caseSensitive, _defaultAnswer, _answerMap, _hiddenAnswerMap, _question, _in, _out);
		}

		public PromptBuilder<T> setDefaultAnswer(Optional<String> answer) {
			_defaultAnswer = answer;

			return this;
		}

		public PromptBuilder<T> setDefaultAnswer(String answer) {
			_defaultAnswer = Optional.ofNullable(answer);

			return this;
		}

		public PromptBuilder<T> withCaseSensitivity(boolean caseSensitivity) {
			_caseSensitive = caseSensitivity;

			return this;
		}

		public PromptBuilder<T> withIn(InputStream in) {
			_in = in;

			return this;
		}

		public PromptBuilder<T> withOut(PrintStream out) {
			_out = out;

			return this;
		}

		private Map<String, T> _answerMap = new HashMap<>();
		private boolean _caseSensitive = false;
		private Optional<String> _defaultAnswer = Optional.empty();
		private Map<String, T> _hiddenAnswerMap = new HashMap<>();
		private InputStream _in = System.in;
		private PrintStream _out = System.out;
		private String _question;

	}

	private PromptUtil(
		boolean caseSensitive, Optional<String> defaultAnswer, Map<String, T> answerMap, Map<String, T> hiddenAnswerMap,
		String question, InputStream in, PrintStream out) {

		_caseSensitive = caseSensitive;
		_defaultAnswer = defaultAnswer;
		_answerMap = answerMap;
		_hiddenAnswerMap = hiddenAnswerMap;
		_question = question;
		_in = in;
		_out = out;
	}

	private String _buildPromptForQuestion(Optional<String> defaultAnswer) {
		boolean bigList = false;

		if (_answerMap.size() > 4) {
			bigList = true;
		}

		StringBuilder stringBuilder = new StringBuilder();

		List<String> answers = new ArrayList<>(_answerMap.keySet());

		Collections.sort(answers, Collections.reverseOrder());

		if (bigList) {
			stringBuilder.append("-------" + System.lineSeparator());

			for (String answer : answers) {
				stringBuilder.append(answer + System.lineSeparator());
			}

			stringBuilder.append("-------");
		}
		else {
			stringBuilder.append("[ ");

			boolean first = true;

			for (String answer : answers) {
				if (!first) {
					stringBuilder.append(" / ");
				}
				else {
					first = false;
				}

				stringBuilder.append(answer);
			}

			stringBuilder.append(" ]");
		}

		if (defaultAnswer.isPresent()) {
			stringBuilder.append(System.lineSeparator());
			stringBuilder.append("Default Answer is: [ " + defaultAnswer.get() + " ]");
			stringBuilder.append(System.lineSeparator());
		}

		return stringBuilder.toString();
	}

	private Optional<T> _checkEntrySetForKey(Map<String, T> map, String decision) {
		Optional<T> answer = Optional.empty();

		for (Entry<String, T> e : map.entrySet()) {
			String potentialAnswer = e.getKey();

			if (!_caseSensitive) {
				potentialAnswer = potentialAnswer.toLowerCase();
			}

			if (Objects.equals(decision, potentialAnswer)) {
				answer = _getValueFromAnswer(e);
			}
		}

		return answer;
	}

	private Optional<T> _findAnswer(String decision) {
		Optional<T> answer;

		if (!_answerMap.isEmpty() || _hiddenAnswerMap.isEmpty()) {
			answer = _checkEntrySetForKey(_answerMap, decision);

			if (!answer.isPresent()) {
				answer = _checkEntrySetForKey(_hiddenAnswerMap, decision);
			}
		}
		else {
			try {
				answer = Optional.of((T)decision);
			}
			catch (ClassCastException cce) {
				throw new UnsupportedOperationException(_NO_ANSWERS_ERROR, cce);
			}
		}

		return answer;
	}

	private String _getDefaultAnswerString() {
		String value = _defaultAnswer.get();

		if (Objects.nonNull(value) && !_caseSensitive) {
			value = value.toLowerCase();
		}

		return value;
	}

	private Optional<T> _getValueFromAnswer(Entry<String, T> e) {
		Optional<T> answer;
		T value = e.getValue();

		if (Objects.nonNull(value) && !_caseSensitive && value instanceof String) {
			String stringValue = value.toString();

			stringValue = stringValue.toLowerCase();

			value = (T)stringValue;
		}

		answer = Optional.of(value);

		return answer;
	}

	private static final String _NO_ANSWERS_ERROR =
		"PromptUtil<T> T must be of type \"String\" for this functionality, " + System.lineSeparator() +
			"or answers must be provided with PromptBuilder<T>";

	private Map<String, T> _answerMap;
	private boolean _caseSensitive;
	private Optional<String> _defaultAnswer;
	private Map<String, T> _hiddenAnswerMap;
	private InputStream _in;
	private PrintStream _out;
	private String _question;

}