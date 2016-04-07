/*
 * Copyright 2016 Matthew Tamlin
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

package com.matthewtamlin.sliding_intro_screen_library;

import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.HashMap;

/**
 * A transformer for apply a custom parallax effect to the Views in a ViewPager. Providing a
 * resource id and a parallax factor causes every view in the view pager with that resource id to
 * have the parallax effect applied to it. If any page does not contain that particular view, the
 * effect is ignored on that page. This allows pages with different layouts to be transformed with
 * this class.
 */
public class MultiviewParallaxTransformer implements ViewPager.PageTransformer {
	/**
	 * Stores the resource id for each view to transform, and maps each id to a parallax effect
	 * factor.
	 */
	private final HashMap<Integer, Float> parallaxFactors = new HashMap<>();

	/**
	 * Maps the root view of each page to a SavedViewUtility so that view references can be
	 * efficiently retrieved.
	 */
	private final HashMap<View, SavedViewUtility> savedViews = new HashMap<>();

	@Override
	public void transformPage(final View page, final float position) {
		final boolean pageIsSelected = (position == 0f);
		final boolean pageIsScrolling = (position > -1f && position < 1f);

		if (pageIsSelected) {
			page.invalidate();
		} else if (pageIsScrolling) {
			for (final Integer id : parallaxFactors.keySet()) {
				final View viewToTransform = getViewToTransform(page, id);

				if (viewToTransform != null) {
					final float parallaxFactor = parallaxFactors.get(id);
					viewToTransform
							.setTranslationX(page.getWidth() * position * parallaxFactor / 2);
				}
			}
		}
	}

	/**
	 * Constructs a new MultiviewParallaxTransformer instance.
	 *
	 * @return the new instance, not null
	 */
	public static MultiviewParallaxTransformer newInstance() {
		return new MultiviewParallaxTransformer();
	}

	/**
	 * Sets this MultiviewParallaxTransformer to apply a parallax effect to all Views with the
	 * provided resource id.
	 *
	 * @param id
	 * 		the resource id of the view to apply the parallax effect to
	 * @param parallaxFactor
	 * 		determines how fast the view should scrol
	 * @return this MultiviewParallaxTransformer
	 */
	public MultiviewParallaxTransformer withParallaxView(int id, float parallaxFactor) {
		parallaxFactors.put(id, parallaxFactor);
		savedViews.clear(); // Recache all views to be safe
		return this;
	}

	/**
	 * Removes a parallax effect from all Views with the provided resource id.
	 *
	 * @param id
	 * 		the resource if of the Views to remove the effect from
	 * @return this MultiviewParallaxTransformer
	 */
	public MultiviewParallaxTransformer withoutParallaxView(int id) {
		parallaxFactors.remove(id);
		return this;
	}

	/**
	 * Returns a reference to the child view of {@code rootView} with the resource id of {@code id}.
	 * Using this method is more efficient that frequent calls to {@link View#findViewById(int)}.
	 *
	 * @param rootView
	 * 		the view to get the child view from, not null
	 * @param id
	 * 		the resource id of the child view
	 * @return the child view of {@code rootView} with the resource id of {@code id}, or null if no
	 * such child view exists
	 */
	public View getViewToTransform(View rootView, int id) {
		if (rootView == null) {
			throw new IllegalArgumentException("rootView cannot be null");
		}

		if (!savedViews.containsKey(rootView)) {
			savedViews.put(rootView, new SavedViewUtility(rootView));
		}

		return savedViews.get(rootView).getChildView(id);
	}

}

/**
 * A utility for efficiently retrieving the children of a View. Using this class is more efficient
 * that frequently calling {@link View#findViewById(int)}.
 */
class SavedViewUtility {
	/**
	 * The view to retrieve the children from.
	 */
	private final View rootView;

	/**
	 * Stores the child Views of {@code view}. Each child view is mapped by its resource id.
	 */
	private final HashMap<Integer, View> cachedViews = new HashMap<>();

	/**
	 * Constructs a new SavedViewUtility instance. The View passed as an argument is set as the root
	 * view of this utility.
	 *
	 * @param rootView
	 * 		the View to get the children of with this utility, not null
	 */
	public SavedViewUtility(View rootView) {
		if (rootView == null) {
			throw new IllegalArgumentException("rootView cannot be null");
		}

		this.rootView = rootView;
	}

	/**
	 * Provides efficient access to the child Views of the root view of this utility.
	 *
	 * @param id
	 * 		the resource id of the view to get
	 * @return the child view which has the provided resource id, or null if no such child view
	 * exists
	 */
	public final View getChildView(final int id) {
		if (cachedViews.containsKey(id)) {
			return cachedViews.get(id);
		} else {
			cachedViews.put(id, rootView.findViewById(id));
			return cachedViews.get(id);
		}
	}

	/**
	 * @return the View which is queried to get the child Views
	 */
	public View getRootView() {
		return rootView;
	}

	/**
	 * Calling this method will force each view to be retrieved using {@link View#findViewById(int)}
	 * next time {@link #getChildView(int)} is called for that view.
	 */
	public void reset() {
		cachedViews.clear();
	}
}