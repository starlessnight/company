package com.metropia.ui;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.metropia.activities.R;
import com.metropia.models.Reservation;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

public class SwipeDeleteTouchListener implements View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private OnDismissCallback mCallback;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    private int mDismissAnimationRefCount = 0;
    private float mDownX;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;
    private View mDownView;
    private LinearLayout parentView;
    private boolean mPaused;
    private int dismissPosition = -1;

    /**
* The callback interface used by {@link SwipeDismissListViewTouchListener} to inform its client
* about a successful dismissal of one or more list item positions.
*/
    public interface OnDismissCallback {
        /**
* Called when the user has indicated they she would like to dismiss one or more list item
* positions.
*
* @param listView The originating {@link ListView}.
* @param reverseSortedPositions An array of positions to dismiss, sorted in descending
* order for convenience.
*/
        void onDismiss(View tripInfoView, List<Reservation> remainReservs);
        
        void onDismissRight();
        
    }

    /**
* Constructs a new swipe-to-dismiss touch listener for the given list view.
*
* @param listView The list view whose items should be dismissable.
* @param callback The callback to trigger when the user has indicated that she would like to
* dismiss one or more list items.
*/
    public SwipeDeleteTouchListener(LinearLayout listContainer, OnDismissCallback callback) {
        ViewConfiguration vc = ViewConfiguration.get(listContainer.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listContainer.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        parentView = listContainer;
        mCallback = callback;
    }

    /**
* Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
*
* @param enabled Whether or not to watch for gestures.
*/
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    /**
* Returns an {@link android.widget.AbsListView.OnScrollListener} to be added to the
* {@link ListView} using
* {@link ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)}.
* If a scroll listener is already assigned, the caller should still pass scroll changes
* through to this listener. This will ensure that this
* {@link SwipeDismissListViewTouchListener} is paused during list view scrolling.</p>
*
* @see {@link SwipeDismissListViewTouchListener}
*/
    public AbsListView.OnScrollListener makeScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        };
    }

    boolean scrolled = false;
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = parentView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
            	scrolled = false;
                if (mPaused) {
                    return false;
                }

                // TODO: ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = parentView.getChildCount();
                int[] listViewCoords = new int[2];
                parentView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = parentView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y) && child.getTag() != null) {
                    	dismissPosition = i;
                        mDownView = child;
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                    view.onTouchEvent(motionEvent);
                    return true;
                }
                dismissPosition = -1;
                mDownView = null;
                mViewWidth = 1;
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    return false;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                float velocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissLeft = false;
                int dismissWidth = mViewWidth/2;
                if (Math.abs(deltaX) > dismissWidth) {
                    dismiss = true;
                    dismissLeft = deltaX < 0;
                } else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity
                        && velocityY < velocityX) {
                    dismiss = true;
                    dismissLeft = mVelocityTracker.getXVelocity() < 0;
                }
                if (dismiss && dismissLeft) {
                    // dismiss
	                final View downView = mDownView; // mDownView gets null'd before animation ends
	                ++mDismissAnimationRefCount;
	                animate(mDownView)
	                        .translationX(-mViewWidth)
	                        .alpha(0)
	                        .setDuration(mAnimationTime)
	                        .setListener(new AnimatorListenerAdapter() {
	                            @Override
	                            public void onAnimationEnd(Animator animation) {
	                             	performDismiss(downView, dismissPosition);
	                            }
	                        });
                } else {
                    // cancel
                    animate(mDownView)
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker = null;
                mDownX = 0;
                mDownView = null;
                mSwiping = false;
                int[] buttons = {R.id.reservation_start_button, R.id.reschedule_panel, R.id.reservation_on_my_way};
            	if (scrolled && ArrayUtils.contains(buttons, view.getId())) return true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    return false;
                }
                mVelocityTracker.addMovement(motionEvent);
                float deltaX = mDownX - motionEvent.getRawX();
                boolean dismissLeft = deltaX > 0;
                if(dismissLeft) {
	                if (Math.abs(deltaX) > mSlop) {
	                	scrolled = true;
	                    mSwiping = true;
	                    parentView.requestDisallowInterceptTouchEvent(true);
	
	                    // Cancel ListView's touch (un-highlighting the item)
	                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
	                            (motionEvent.getActionIndex()
	                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
	                    parentView.onTouchEvent(cancelEvent);
	                }
	
	                if (mSwiping) {
	                    setTranslationX(mDownView, -deltaX);
//	                    setAlpha(mDownView, Math.max(0f, Math.min(1f,
//	                            1f - 2f * Math.abs(deltaX) / mViewWidth)));
	                    return true;
	                }
                }
                break;
            }
        }
        return false;
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --mDismissAnimationRefCount;
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(mPendingDismisses);

                    int[] dismissPositions = new int[mPendingDismisses.size()];
                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                        dismissPositions[i] = mPendingDismisses.get(i).position;
                    }
                    mCallback.onDismiss(dismissView, getRemainReservations(dismissView));

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        // Reset view presentation
                        setAlpha(pendingDismiss.view, 1f);
                        setTranslationX(pendingDismiss.view, 0);
                        lp = pendingDismiss.view.getLayoutParams();
                        lp.height = originalHeight;
                        pendingDismiss.view.setLayoutParams(lp);
                    }

                    mPendingDismisses.clear();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }
    
    private List<Reservation> getRemainReservations(View dismissView) {
    	List<Reservation> remains = new ArrayList<Reservation>();
    	for(int i = 0 ; i < parentView.getChildCount() ; i++) {
    		View child = parentView.getChildAt(i);
    		if(child.getTag() != null && !Long.valueOf(((Reservation)child.getTag()).getRid()).equals(((Reservation)dismissView.getTag()).getRid())) {
    			remains.add((Reservation)child.getTag());
    		}
    	}
    	return remains;
    }
    
//    private void performDismissParent(final View mView) {
//        final ViewGroup.LayoutParams lp = mView.getLayoutParams();
//        final int originalHeight = mView.getHeight();
//
//        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
//
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mCallback.onDismissParent();
//                // Reset view presentation
//                setAlpha(mView, 1f);
//                setTranslationX(mView, 0);
//                lp.height = originalHeight;
//                mView.setLayoutParams(lp);
//            }
//        });
//
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                lp.height = (Integer) valueAnimator.getAnimatedValue();
//                mView.setLayoutParams(lp);
//            }
//        });
//
//        animator.start();
//    }
}
