package com.seungju.quiz.restriction.repository;

import com.seungju.quiz.restriction.constant.ConditionType;
import com.seungju.quiz.restriction.constant.ConnectType;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import org.hibernate.validator.internal.engine.groups.Group;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class Restriction {

	private final List<Condition> conditions = new ArrayList<>();
	private final List<Restriction> children = new ArrayList<>();
	private final List<FetchJoin> fetchList = new ArrayList<>();
	private final ConnectType conn;

	public Restriction() {
		this.conn = ConnectType.AND;
	}

	public Restriction(ConnectType conn) {
		this.conn = conn;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <Y> Path<Y> getPath(Root root, String key, CriteriaQuery<?> query) {
		Path<Y> x = null;
		if (key.contains(".")) {
			String[] keySplit = key.split("\\.");
			int count = 1;
			Set<? extends Fetch<?, ?>> fetcheList = root.getFetches();
			Set<? extends Join<?, ?>> joinList = root.getJoins();
			Join<?, ?> j = null;

			for (String k : keySplit) {
				if (count != keySplit.length) {
					Fetch<?, ?> fetchCheck = fetcheList.stream()
							.filter(c -> c.getAttribute().getName().equals(k))
							.findAny()
							.orElse(null);

					if (fetchCheck != null) {
						fetcheList = fetchCheck.getFetches();
						j = (Join<?, ?>) fetchCheck;
					} else {
						if (j != null) {
							j = j.join(k, JoinType.LEFT);
						} else {
							Join<?, ?> joinCheck = joinList.stream()
									.filter(c -> c.getAttribute().getName().equals(k))
									.findAny()
									.orElse(null);
							if (joinCheck != null) {
								joinList = joinCheck.getJoins();
								j = joinCheck;
							} else {
								j = root.join(k, JoinType.LEFT);
							}
						}
					}
				} else {
					x = j.get(k);
				}

				if (isCollectionType(j.getClass())) {
					query.distinct(true); // 카테시안 곱 방지
				}

				count++;
			}
		} else {
			x = root.get(key);
		}
		return x;
	}

	private static boolean isCollectionType(Class<?> clazz) {
		return Set.class.isAssignableFrom(clazz)
				|| List.class.isAssignableFrom(clazz)
				|| SetAttribute.class.isAssignableFrom(clazz)
				|| ListAttribute.class.isAssignableFrom(clazz);
	}

	public void clear() {
		this.fetchList.clear();
		this.children.clear();
		this.conditions.clear();
	}

	public void childrenClear() {
		this.children.clear();
	}

	public void conditionsClear() {
		this.conditions.clear();
	}

	public void addChild(Restriction child) {
		this.children.add(child);
	}

	public void addFetchJoin(String fieldName, JoinType joinType) {
		this.fetchList.add(new FetchJoin(fieldName, joinType));
	}

	public void removeFetchJoin(String fieldName) {
		List<FetchJoin> fetchJoinList = this.fetchList.stream()
				.filter(fetchJoin -> !fetchJoin.getFieldName().equals(fieldName))
				.toList();
		this.fetchList.clear();
		this.fetchList.addAll(fetchJoinList);
	}

	public Restriction eq(String name, Object value) {
		conditions.add(new Condition(ConditionType.EQUALS, name, value));
		return this;
	}

	public Restriction le(String name, Object value) {
		conditions.add(new Condition(ConditionType.LESS_THAN_OR_EQUAL_TO, name, value));
		return this;
	}

	public Restriction lne(String name, Object value) {
		conditions.add(new Condition(ConditionType.LESS_THAN_NOT_EQUAL_TO, name, value));
		return this;
	}

	public Restriction ge(String name, Object value) {
		conditions.add(new Condition(ConditionType.GREATER_THAN_OR_EQUAL_TO, name, value));
		return this;
	}

	public Restriction gne(String name, Object value) {
		conditions.add(new Condition(ConditionType.GREATER_THAN_NOT_EQUAL_TO, name, value));
		return this;
	}

	public Restriction isNull(String key) {
		conditions.add(new Condition(ConditionType.IS_NULL, key));
		return this;
	}

	public Restriction isNotNull(String key) {
		conditions.add(new Condition(ConditionType.IS_NOT_NULL, key));
		return this;
	}

	public Restriction ne(String key, Object object) {
		conditions.add(new Condition(ConditionType.NOT_EQUAL, key, object));
		return this;
	}

	public Restriction like(String key, String object) {
		conditions.add(new Condition(ConditionType.LIKE, key, object));
		return this;
	}

	public Restriction ilike(String key, String object) {
		conditions.add(new Condition(ConditionType.ILIKE, key, object));
		return this;
	}

	public Restriction nlike(String key, String object) {
		conditions.add(new Condition(ConditionType.NOT_LIKE, key, object));
		return this;
	}

	public Restriction nilike(String key, String object) {
		conditions.add(new Condition(ConditionType.NOT_ILIKE, key, object));
		return this;
	}

	public Restriction between(String key, Object start, Object end) {
		conditions.add(new Condition(ConditionType.BETWEEN, key, start, end));
		return this;
	}

	@SuppressWarnings("rawtypes")
	public void in(String name, Collection value) {
		conditions.add(new Condition(ConditionType.IN, name, value));
	}

	@SuppressWarnings("rawtypes")
	public void strIn(String name, Collection value) {
		conditions.add(new Condition(ConditionType.STR_IN, name, value));
	}

	@SuppressWarnings("rawtypes")
	public void notIn(String name, Collection value) {
		conditions.add(new Condition(ConditionType.NOT_IN, name, value));
	}

	public void eqProperty(String key, String key2) {
		conditions.add(new Condition(ConditionType.EQUAL_PROPERTY, key, key2));
	}

	public void neProperty(String key, String key2) {
		conditions.add(new Condition(ConditionType.NOT_EQUAL_PROPERTY, key, key2));
	}

	public void eqTrim(String name, Object value) {
		conditions.add(new Condition(ConditionType.EQUALS_TRIM, name, value));
	}

	public void neTrim(String name, Object value) {
		conditions.add(new Condition(ConditionType.NOT_EQUALS_TRIM, name, value));
	}

	public void isEmpty(String name) {
		conditions.add(new Condition(ConditionType.IS_EMPTY, name));
	}

	public void isNotEmpty(String name) {
		conditions.add(new Condition(ConditionType.IS_NOT_EMPTY, name));
	}

	@SuppressWarnings("serial")
	public <T> Specification<T> output() {

		Specification<T> spec = (root, query, cb) -> {
			// fetchJoin check
			if (!fetchList.isEmpty()) {
				if (root.getJavaType().equals(query.getResultType())) { // data query 와 count query 를 구분
					for (FetchJoin fetchJoin : fetchList) {
						if (fetchJoin.fieldName.contains(".")) {
							Set<? extends Fetch<?, ?>> fetches = root.getFetches();
							Fetch<?, ?> fetch = null;
							for (String field : fetchJoin.fieldName.split("\\.")) {
								if (fetches.stream().anyMatch(x -> x.getAttribute().getName().equals(field))) {
									fetch = fetches.stream()
											.filter(x -> x.getAttribute().getName().equals(field))
											.findAny()
											.get();
								} else {
									fetch = Objects.requireNonNullElse(fetch, root).fetch(field, fetchJoin.joinType);
								}

								if (isCollectionType(fetch.getClass())) {
									query.distinct(true); // 카테시안 곱 방지
								}

								fetches = fetch.getFetches();
							}
						} else {
							if (root.getFetches()
									.stream()
									.anyMatch(x -> x.getAttribute().getName().equals(fetchJoin.fieldName))) {
								continue;
							}
							Fetch<?, ?> fetch = root.fetch(fetchJoin.fieldName, fetchJoin.joinType);

							if (isCollectionType(fetch.getClass())) {
								query.distinct(true); // 카테시안 곱 방지
							}
						}
					}
				}
			}

			List<Predicate> items = new ArrayList<>();
			for (Condition condition : conditions) {
				String key = condition.getName().toString();
				Object object = condition.getValue1();
				Object object2 = condition.getValue2();

				items.addAll(getPredicateList(condition.getType(), cb, root, key, object, object2, query));
			}

			if (items.size() > 1) {
				Predicate[] ps = items.toArray(new Predicate[]{});
				return (conn == ConnectType.AND) ? cb.and(ps) : cb.or(ps);
			} else if (items.size() == 1) {
				return items.get(0);
			}

			return null;
		};

		if (!children.isEmpty()) {
			for (Restriction child : children) {
				spec = (conn == ConnectType.AND) ? spec.and(child.output()) : spec.or(child.output());
			}
		}

		return spec;
	}

	private List<Predicate> getPredicateList(ConditionType conditionType, CriteriaBuilder cb, Root<?> root, String key,
											 Object object, Object object2, CriteriaQuery<?> query) {
		List<Predicate> items = new ArrayList<>();
		switch (conditionType) {
			case EQUALS: {
				items.add(cb.equal(getPath(root, key, query), object));
				break;
			}
			case EQUAL_PROPERTY: {
				items.add(cb.equal(getPath(root, key, query), getPath(root, object.toString(), query)));
				break;
			}
			case NOT_EQUAL_PROPERTY: {
				items.add(cb.notEqual(getPath(root, key, query), getPath(root, object.toString(), query)));
				break;
			}
			case NOT_EQUAL: {
				items.add(cb.notEqual(getPath(root, key, query), object));
				break;
			}
			case LIKE: {
				items.add(cb.like(getPath(root, key, query), (String) object));
				break;
			}
			case ILIKE: {
				items.add(cb.like(cb.lower(getPath(root, key, query)), ((String) object).toLowerCase()));
				break;
			}
			case NOT_LIKE: {
				items.add(cb.not(cb.like(getPath(root, key, query), (String) object)));
				break;
			}
			case NOT_ILIKE: {
				items.add(cb.not(cb.like(cb.lower(getPath(root, key, query)), ((String) object).toLowerCase())));
				break;
			}
			case IS_NULL: {
				items.add(cb.isNull(getPath(root, key, query)));
				break;
			}
			case IS_NOT_NULL: {
				items.add(cb.isNotNull(getPath(root, key, query)));
				break;
			}
			case STR_IN: {
				items.addAll(getPredicateItemStrIn(cb, root, key, object, query));
				break;
			}
			case IN: {
				final Path<Group> group = getPath(root, key, query);
				if (group != null) {
					items.add(group.in((Collection<?>) object));
				}
				break;
			}
			case NOT_IN: {
				final Path<Group> group = getPath(root, key, query);
				if (group != null) {
					items.add(group.in((Collection) object).not());
				}
				break;
			}
			case LESS_THAN_OR_EQUAL_TO: {
				items.addAll(getPredicateItemLessThen(cb, root, key, object, true, query));
				break;
			}
			case LESS_THAN_NOT_EQUAL_TO: {
				items.addAll(getPredicateItemLessThen(cb, root, key, object, false, query));
				break;
			}
			case GREATER_THAN_OR_EQUAL_TO: {
				items.addAll(getPredicateItemGreaterThen(cb, root, key, object, true, query));
				break;
			}
			case GREATER_THAN_NOT_EQUAL_TO: {
				items.addAll(getPredicateItemGreaterThen(cb, root, key, object, false, query));
				break;
			}

			case BETWEEN: {
				items.addAll(getPredicateItemBetween(cb, root, key, object, object2, query));
				break;
			}

			case EQUALS_TRIM: {
				items.add(cb.equal(cb.trim(getPath(root, key, query)), object));
				break;
			}

			case NOT_EQUALS_TRIM: {
				items.add(cb.notEqual(cb.trim(getPath(root, key, query)), object));
				break;
			}

			case IS_EMPTY: {
				items.add(cb.isEmpty(getPath(root, key, query)));
				break;
			}

			case IS_NOT_EMPTY: {
				items.add(cb.isNotEmpty(getPath(root, key, query)));
				break;
			}

			default:
				break;
		}

		return items;
	}

	private List<Predicate> getPredicateItemLessThen(CriteriaBuilder cb, Root<?> root, String key, Object object,
													 boolean isEquals, CriteriaQuery<?> query) {
		List<Predicate> items = new ArrayList<>();

		if (isEquals) {
			if (object instanceof Date) {
				items.add(cb.lessThanOrEqualTo(Restriction.getPath(root, key, query), (Date) object));
			} else if (object instanceof LocalDateTime) {
				items.add(cb.lessThanOrEqualTo(Restriction.getPath(root, key, query), (LocalDateTime) object));
			} else if (object instanceof LocalDate) {
				items.add(cb.lessThanOrEqualTo(Restriction.getPath(root, key, query), (LocalDate) object));
			} else if (object instanceof LocalTime) {
				items.add(cb.lessThanOrEqualTo(Restriction.getPath(root, key, query), (LocalTime) object));
			} else {
				items.add(cb.le(Restriction.getPath(root, key, query), (Number) object));
			}
		} else {
			if (object instanceof Date) {
				items.add(cb.lessThan(Restriction.getPath(root, key, query), (Date) object));
			} else if (object instanceof LocalDateTime) {
				items.add(cb.lessThan(Restriction.getPath(root, key, query), (LocalDateTime) object));
			} else if (object instanceof LocalDate) {
				items.add(cb.lessThan(Restriction.getPath(root, key, query), (LocalDate) object));
			} else if (object instanceof LocalTime) {
				items.add(cb.lessThan(Restriction.getPath(root, key, query), (LocalTime) object));
			} else {
				items.add(cb.lt(Restriction.getPath(root, key, query), (Number) object));
			}
		}

		return items;
	}

	private List<Predicate> getPredicateItemGreaterThen(CriteriaBuilder cb, Root<?> root, String key, Object object,
														boolean isEquals, CriteriaQuery<?> query) {
		List<Predicate> items = new ArrayList<>();

		if (isEquals) {
			if (object instanceof Date) {
				items.add(cb.greaterThanOrEqualTo(Restriction.getPath(root, key, query), (Date) object));
			} else if (object instanceof LocalDateTime) {
				items.add(cb.greaterThanOrEqualTo(Restriction.getPath(root, key, query), (LocalDateTime) object));
			} else if (object instanceof LocalDate) {
				items.add(cb.greaterThanOrEqualTo(Restriction.getPath(root, key, query), (LocalDate) object));
			} else if (object instanceof LocalTime) {
				items.add(cb.greaterThanOrEqualTo(Restriction.getPath(root, key, query), (LocalTime) object));
			} else {
				items.add(cb.ge(Restriction.getPath(root, key, query), (Number) object));
			}
		} else {
			if (object instanceof Date) {
				items.add(cb.greaterThan(Restriction.getPath(root, key, query), (Date) object));
			} else if (object instanceof LocalDateTime) {
				items.add(cb.greaterThan(Restriction.getPath(root, key, query), (LocalDateTime) object));
			} else if (object instanceof LocalDate) {
				items.add(cb.greaterThan(Restriction.getPath(root, key, query), (LocalDate) object));
			} else if (object instanceof LocalTime) {
				items.add(cb.greaterThan(Restriction.getPath(root, key, query), (LocalTime) object));
			} else {
				items.add(cb.gt(Restriction.getPath(root, key, query), (Number) object));
			}
		}

		return items;
	}

	private List<Predicate> getPredicateItemBetween(CriteriaBuilder cb, Root<?> root, String key, Object object,
													Object object2, CriteriaQuery<?> query) {
		List<Predicate> items = new ArrayList<>();

		if (object instanceof Date) {
			items.add(cb.between(Restriction.getPath(root, key, query), (Date) object, (Date) object2));
		} else if (LocalDateTime.class.isAssignableFrom(object.getClass())) {
			items.add(
					cb.between(Restriction.getPath(root, key, query), (LocalDateTime) object, (LocalDateTime) object2));
		} else if (LocalDate.class.isAssignableFrom(object.getClass())) {
			items.add(cb.between(Restriction.getPath(root, key, query), (LocalDate) object, (LocalDate) object2));
		} else if (LocalTime.class.isAssignableFrom(object.getClass())) {
			items.add(cb.between(Restriction.getPath(root, key, query), (LocalTime) object, (LocalTime) object2));
		} else {
			items.add(
					cb.between(Restriction.getPath(root, key, query), (Double) object, (Double) object2));
		}

		return items;
	}

	private List<Predicate> getPredicateItemStrIn(CriteriaBuilder cb, Root<?> root, String key, Object object,
												  CriteriaQuery<?> query) {
		List<Predicate> items = new ArrayList<>();
		Predicate p = null;
		Path<String> field = getPath(root, key, query);
		if (field != null) {
			for (Object o : (Collection) object) {
				Predicate tempP = cb.like(field.as(String.class), "%" + o + "%");
				p = (p == null) ? tempP : cb.or(p, tempP);
			}
			items.add(p);
		}

		return items;
	}
}

