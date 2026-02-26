package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petserver.entity.*;
import com.example.petserver.vo.ProductRecommendVO;
import com.example.petserver.mapper.ProductMapper;
import com.example.petserver.mapper.OrdersMapper;
import com.example.petserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, com.example.petserver.entity.Product> implements IProductService {

	@Autowired
	private IPetInfoService petInfoService;
	@Autowired
	private IHealthRecordService healthRecordService;
	@Autowired
	private IFeedPlanService feedPlanService;
	@Autowired
	private OrdersMapper ordersMapper;

	@Override
	public List<ProductRecommendVO> getRecommendProductsVO(Long userId) {
		// 1) 用户宠物
		List<PetInfo> pets = petInfoService.list(new QueryWrapper<PetInfo>().eq("user_id", userId));

		// 无宠物：返回上架且最新的商品
		if (pets == null || pets.isEmpty()) {
			List<Product> list = this.list(new QueryWrapper<Product>()
					.eq("status", 1)
					.orderByDesc("id")
					.last("LIMIT 8"));
			return list.stream().map(p -> {
				ProductRecommendVO vo = new ProductRecommendVO();
				org.springframework.beans.BeanUtils.copyProperties(p, vo);
				vo.setRecommendReason("新品上架");
				vo.setMatchScore(0);
				return vo;
			}).collect(Collectors.toList());
		}

		// 2) 构建关键词画像（去重）
		Set<String> keywords = new HashSet<>();
		for (PetInfo pet : pets) {
			keywords.addAll(extractKeywordsForPet(pet));
		}

		// 3) 历史订单偏好与去重
		List<Orders> paid = ordersMapper.selectList(new QueryWrapper<Orders>()
				.eq("user_id", userId)
				.in("status", 1, 2, 3));
		Set<Long> purchasedIds = paid.stream().map(Orders::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
		List<String> topCategories = computeTopCategories(paid);

		Integer exclusiveType = determineExclusiveType(pets);

		// 4) 召回+补充
		List<Product> candidates = recallByKeywords(keywords, purchasedIds, exclusiveType);
		if (candidates.size() < 8 && !topCategories.isEmpty()) {
			candidates = mergeCandidates(candidates, recallByCategories(topCategories, purchasedIds, exclusiveType));
		}

		// 5) 排序与转换
		return candidates.stream()
				.map(p -> calculateScoreAndReason(p, keywords, topCategories, pets))
				.sorted((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()))
				.limit(8)
				.collect(Collectors.toList());
	}

	private ProductRecommendVO calculateScoreAndReason(Product p, Set<String> keywords, List<String> topCategories, List<PetInfo> pets) {
		ProductRecommendVO vo = new ProductRecommendVO();
		org.springframework.beans.BeanUtils.copyProperties(p, vo);

		int score = 0;
		List<String> reasons = new ArrayList<>();
		String tags = Optional.ofNullable(p.getTags()).orElse("");

		Set<String> breeds = pets.stream().map(PetInfo::getBreed).filter(Objects::nonNull).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
		boolean anySterilized = pets.stream().anyMatch(pi -> pi.getSterilization() != null && pi.getSterilization() == 1);
		boolean hasDog = pets.stream().anyMatch(pi -> pi.getType() != null && pi.getType() == 0);
		boolean hasCat = pets.stream().anyMatch(pi -> pi.getType() != null && pi.getType() == 1);

		if (tags.contains("关节护理")) {
			score += 5;
			reasons.add("针对关节护理");
		} else if (tags.contains("肠胃敏感")) {
			score += 5;
			reasons.add("针对肠胃敏感");
		} else if (tags.contains("皮肤护理")) {
			score += 5;
			reasons.add("针对皮肤护理");
		} else if (tags.contains("牙齿护理")) {
			score += 4;
			reasons.add("牙齿护理强化");
		} else if (tags.contains("低脂")) {
			score += 4;
			reasons.add("控制体重推荐");
		}

		if (reasons.isEmpty()) {
			if (tags.contains("幼")) {
				score += 3;
				reasons.add(hasCat ? "适合幼猫" : "适合幼犬");
			} else if (tags.contains("老")) {
				score += 3;
				reasons.add(hasCat ? "适合老年猫" : "适合老年犬");
			} else if (tags.contains("成")) {
				score += 2;
				reasons.add(hasCat ? "适合成猫" : "适合成犬");
			}
		}

		if (reasons.isEmpty()) {
			for (String b : breeds) {
				if (tags.contains(b)) {
					score += 2;
					reasons.add("适合" + b);
					break;
				}
			}
		}

		if (reasons.isEmpty() && anySterilized && tags.contains("绝育")) {
			score += 3;
			reasons.add("适合已绝育宠物");
		}

		if (p.getCategory() != null && topCategories.contains(p.getCategory())) {
			score += 1;
			if (reasons.isEmpty()) {
				String cn = translateCategory(p.getCategory());
				reasons.add("根据你常购的" + cn + "推荐");
			}
		}

		if (reasons.isEmpty()) {
			reasons.add("店长力荐");
		}

		vo.setMatchScore(score);
		vo.setRecommendReason(reasons.get(0)); // 取第一条最主要的理由
		return vo;
	}

	private String translateCategory(String key) {
		if (key == null) return "商品";
		switch (key) {
			case "food":
				return "主粮";
			case "snack":
				return "零食";
			case "toy":
				return "玩具";
			case "daily":
				return "日用品";
			case "medical":
				return "医疗保健";
			case "device":
				return "宠物器械";
			default:
				return "商品";
		}
	}

	@Override
	public List<Product> getRecommendProducts(Long userId) {
		// 1) 用户宠物
		List<PetInfo> pets = petInfoService.list(new QueryWrapper<PetInfo>().eq("user_id", userId));

		// 无宠物：返回上架且最新的商品
		if (pets == null || pets.isEmpty()) {
			return this.list(new QueryWrapper<Product>()
					.eq("status", 1)
					.orderByDesc("id")
					.last("LIMIT 8"));
		}

		// 2) 构建关键词画像（去重）
		Set<String> keywords = new HashSet<>();
		for (PetInfo pet : pets) {
			keywords.addAll(extractKeywordsForPet(pet));
		}

		// 3) 历史订单偏好与去重
		List<Orders> paid = ordersMapper.selectList(new QueryWrapper<Orders>()
				.eq("user_id", userId)
				.in("status", 1, 2, 3));
		Set<Long> purchasedIds = paid.stream().map(Orders::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
		List<String> topCategories = computeTopCategories(paid);

		Integer exclusiveType = determineExclusiveType(pets);

		// 4) 召回+补充
		List<Product> candidates = recallByKeywords(keywords, purchasedIds, exclusiveType);
		if (candidates.size() < 8 && !topCategories.isEmpty()) {
			candidates = mergeCandidates(candidates, recallByCategories(topCategories, purchasedIds, exclusiveType));
		}

		// 5) 排序与Top-N
		List<Product> ranked = rankByScore(candidates, keywords, topCategories);
		return ranked.stream().limit(8).collect(Collectors.toList());
	}

	@Override
	public List<Product> getRecommendProductsByPet(Long userId, Long petId) {
		// 单宠物画像
		PetInfo pet = petInfoService.getById(petId);
		if (pet == null) {
			return this.list(new QueryWrapper<Product>()
					.eq("status", 1)
					.orderByDesc("id")
					.last("LIMIT 8"));
		}
		Set<String> keywords = extractKeywordsForPet(pet);

		// 购买历史与分类偏好（用于去重与补充）
		List<Orders> paid = ordersMapper.selectList(new QueryWrapper<Orders>()
				.eq("user_id", userId)
				.in("status", 1, 2, 3));
		Set<Long> purchasedIds = paid.stream().map(Orders::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
		List<String> topCategories = computeTopCategories(paid);

		Integer exclusiveType = determineExclusiveType(Collections.singletonList(pet));

		List<Product> candidates = recallByKeywords(keywords, purchasedIds, exclusiveType);
		if (candidates.size() < 8 && !topCategories.isEmpty()) {
			candidates = mergeCandidates(candidates, recallByCategories(topCategories, purchasedIds, exclusiveType));
		}
		List<Product> ranked = rankByScore(candidates, keywords, topCategories);
		return ranked.stream().limit(8).collect(Collectors.toList());
	}

	@Override
	public List<ProductRecommendVO> getRecommendProductsByPetVO(Long userId, Long petId) {
		PetInfo pet = petInfoService.getById(petId);
		if (pet == null) {
			List<Product> list = this.list(new QueryWrapper<Product>()
					.eq("status", 1)
					.orderByDesc("id")
					.last("LIMIT 8"));
			return list.stream().map(p -> {
				ProductRecommendVO vo = new ProductRecommendVO();
				org.springframework.beans.BeanUtils.copyProperties(p, vo);
				vo.setRecommendReason("新品上架");
				vo.setMatchScore(0);
				return vo;
			}).collect(Collectors.toList());
		}
		Set<String> keywords = extractKeywordsForPet(pet);
		List<Orders> paid = ordersMapper.selectList(new QueryWrapper<Orders>()
				.eq("user_id", userId)
				.in("status", 1, 2, 3));
		Set<Long> purchasedIds = paid.stream().map(Orders::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
		List<String> topCategories = computeTopCategories(paid);

		Integer exclusiveType = determineExclusiveType(Collections.singletonList(pet));

		List<Product> candidates = recallByKeywords(keywords, purchasedIds, exclusiveType);
		if (candidates.size() < 8 && !topCategories.isEmpty()) {
			candidates = mergeCandidates(candidates, recallByCategories(topCategories, purchasedIds, exclusiveType));
		}
		return candidates.stream()
				.map(p -> calculateScoreAndReason(p, keywords, topCategories, Collections.singletonList(pet)))
				.sorted((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()))
				.limit(8)
				.collect(Collectors.toList());
	}

	// ---- 私有帮助方法 ----
	private Set<String> extractKeywordsForPet(PetInfo pet) {
		Set<String> keywords = new HashSet<>();
		if (pet.getType() != null) {
			if (pet.getType() == 0) keywords.add("狗");
			else if (pet.getType() == 1) keywords.add("猫");
		}
		if (pet.getBreed() != null && !pet.getBreed().isEmpty()) {
			keywords.add(pet.getBreed());
		}
		if (pet.getAge() != null) {
			if (pet.getAge() <= 1) keywords.add("幼");
			else if (pet.getAge() >= 10) keywords.add("老");
			else keywords.add("成");
		}
		if (pet.getSterilization() != null && pet.getSterilization() == 1) {
			keywords.add("绝育");
			keywords.add("营养");
		}
		// 健康记录映射标签
		List<HealthRecord> records = healthRecordService.getByPetId(pet.getId());
		for (HealthRecord r : records) {
			String combined = (Optional.ofNullable(r.getType()).orElse("") + " " + Optional.ofNullable(r.getDescription()).orElse("")).toLowerCase();
			if (containsAny(combined, "joint", "关节")) keywords.add("关节护理");
			if (containsAny(combined, "skin", "皮肤")) keywords.add("皮肤护理");
			if (containsAny(combined, "digest", "肠胃", "腹泻", "敏感")) keywords.add("肠胃敏感");
			if (containsAny(combined, "dental", "牙齿")) keywords.add("牙齿护理");
			if (containsAny(combined, "overweight", "肥胖", "体重")) keywords.add("低脂");
		}
		// 喂养计划关键词
		List<FeedPlan> plans = feedPlanService.getByPetId(pet.getId());
		for (FeedPlan plan : plans) {
			String food = Optional.ofNullable(plan.getFoodName()).orElse("");
			for (String token : tokenize(food)) {
				if (token.length() >= 2) keywords.add(token);
			}
		}
		return keywords;
	}

	private List<String> computeTopCategories(List<Orders> paid) {
		Map<String, Long> categoryPref = new HashMap<>();
		for (Orders o : paid) {
			Product p = getById(o.getProductId());
			if (p != null && p.getCategory() != null) {
				categoryPref.put(p.getCategory(), categoryPref.getOrDefault(p.getCategory(), 0L) + 1);
			}
		}
		return categoryPref.entrySet().stream()
				.sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
				.map(Map.Entry::getKey)
				.limit(2)
				.collect(Collectors.toList());
	}

	private Integer determineExclusiveType(List<PetInfo> pets) {
		if (pets == null || pets.isEmpty()) return null;
		boolean hasDog = pets.stream().anyMatch(p -> p.getType() != null && p.getType() == 0);
		boolean hasCat = pets.stream().anyMatch(p -> p.getType() != null && p.getType() == 1);
		if (hasDog && !hasCat) return 0; // Only Dog
		if (!hasDog && hasCat) return 1; // Only Cat
		return null;
	}

	private List<Product> recallByKeywords(Set<String> keywords, Set<Long> purchasedIds, Integer exclusiveType) {
		QueryWrapper<Product> candidateQuery = new QueryWrapper<>();
		candidateQuery.eq("status", 1);
		if (!purchasedIds.isEmpty()) {
			candidateQuery.notIn("id", purchasedIds);
		}

		// 物种排他性过滤
		if (exclusiveType != null) {
			if (exclusiveType == 0) { // Dog Only: Keep (Not Cat OR Has Dog)
				candidateQuery.and(w -> w.notLike("tags", "猫").or().like("tags", "狗"));
			} else if (exclusiveType == 1) { // Cat Only: Keep (Not Dog OR Has Cat)
				candidateQuery.and(w -> w.notLike("tags", "狗").or().like("tags", "猫"));
			}
		}

		if (!keywords.isEmpty()) {
			candidateQuery.and(w -> {
				boolean first = true;
				for (String key : keywords) {
					if (first) {
						w.like("tags", key);
						first = false;
					} else {
						w.or().like("tags", key);
					}
				}
			});
		}
		candidateQuery.last("LIMIT 50");
		return this.list(candidateQuery);
	}

	private List<Product> recallByCategories(List<String> topCategories, Set<Long> purchasedIds, Integer exclusiveType) {
		QueryWrapper<Product> catQuery = new QueryWrapper<>();
		catQuery.eq("status", 1);
		if (!purchasedIds.isEmpty()) catQuery.notIn("id", purchasedIds);
		catQuery.in("category", topCategories);

		if (exclusiveType != null) {
			if (exclusiveType == 0) {
				catQuery.and(w -> w.notLike("tags", "猫").or().like("tags", "狗"));
			} else if (exclusiveType == 1) {
				catQuery.and(w -> w.notLike("tags", "狗").or().like("tags", "猫"));
			}
		}

		catQuery.last("LIMIT 50");
		return this.list(catQuery);
	}

	private List<Product> mergeCandidates(List<Product> base, List<Product> more) {
		Set<Long> seen = base.stream().map(Product::getId).collect(Collectors.toSet());
		for (Product p : more) {
			if (seen.add(p.getId())) base.add(p);
		}
		return base;
	}

	private List<Product> rankByScore(List<Product> candidates, Set<String> keywords, List<String> topCategories) {
		return candidates.stream()
				.sorted((a, b) -> Integer.compare(score(b, keywords, topCategories), score(a, keywords, topCategories)))
				.collect(Collectors.toList());
	}

	private static boolean containsAny(String text, String... keys) {
		for (String k : keys) {
			if (text.contains(k.toLowerCase())) return true;
		}
		return false;
	}

	private static List<String> tokenize(String s) {
		if (s == null || s.isEmpty()) return Collections.emptyList();
		List<String> rawTokens = Arrays.stream(s.replace("/", " ").replace(",", " ").split("\\s+"))
				.map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList());

		List<String> result = new ArrayList<>(rawTokens);
		// 扩展分词：去除常见后缀，提取核心词 (e.g. "三文鱼冻干" -> "三文鱼")
		String[] suffixes = {"冻干", "罐头", "猫粮", "犬粮", "狗粮", "零食", "干粮", "湿粮", "肉"};
		for (String t : rawTokens) {
			for (String suf : suffixes) {
				if (t.length() > suf.length() && t.endsWith(suf)) {
					result.add(t.substring(0, t.length() - suf.length()));
				}
			}
		}
		return result;
	}

	private static int score(Product p, Set<String> keywords, List<String> topCategories) {
		int s = 0;
		String tags = Optional.ofNullable(p.getTags()).orElse("");
		for (String k : keywords) {
			if (!k.isEmpty() && tags.contains(k)) s += 2;
		}
		if (p.getCategory() != null && topCategories.contains(p.getCategory())) s += 1;
		return s;
	}
}
