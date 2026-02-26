package com.example.petserver.vo;

import com.example.petserver.entity.Product;
import lombok.Data;

@Data
public class ProductRecommendVO extends Product {

	private String recommendReason;
	private int matchScore;

	public String getRecommendReason() {
		return recommendReason;
	}

	public void setRecommendReason(String recommendReason) {
		this.recommendReason = recommendReason;
	}

	public int getMatchScore() {
		return matchScore;
	}

	public void setMatchScore(int matchScore) {
		this.matchScore = matchScore;
	}
}
