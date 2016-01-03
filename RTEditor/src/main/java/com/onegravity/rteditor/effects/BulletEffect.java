/*
 * Copyright (C) 2015 Emanuel Moecklin
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

package com.onegravity.rteditor.effects;

import android.text.Spannable;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.BulletSpan;
import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Helper;
import com.onegravity.rteditor.utils.Paragraph;
import com.onegravity.rteditor.utils.Selection;

import java.util.List;

/**
 * Bullet points.
 * <p>
 * BulletSpans are always applied to whole paragraphs and each paragraphs gets its "own" BulletSpan (1:1).
 * Editing might violate this rule (deleting a line feed merges two paragraphs).
 * Each call to applyToSelection will make sure that each paragraph has again its own BulletSpan
 * (call applyToSelection(RTEditText, null, null) and all will be good again).
 */
public class BulletEffect extends ParagraphEffect<Boolean, BulletSpan> {

    private ParagraphSpanProcessor<Boolean> mSpans2Process = new ParagraphSpanProcessor();

    @Override
    public synchronized void applyToSelection(final RTEditText editor, Selection selectedParagraphs, Boolean enable) {
        final Spannable str = editor.getText();

        mSpans2Process.clear();

        for (Paragraph paragraph : editor.getParagraphs()) {

            // find existing spans for this paragraph and add them to spans2Process to be removed
            List<RTSpan<Boolean>> existingSpans = getSpans(str, paragraph, SpanCollectMode.SPAN_FLAGS);
            boolean hasExistingSpans = !existingSpans.isEmpty();
            if (hasExistingSpans) {
                for (RTSpan<Boolean> span : existingSpans) {
                    mSpans2Process.addParagraphSpan(span, paragraph, true);
                }
            }

            // if the paragraph is selected then we sure have a bullet
            boolean hasBullet = paragraph.isSelected(selectedParagraphs) ? enable : hasExistingSpans;

            // if we have a bullet then apply a new span
            if (hasBullet) {
                int margin = Helper.getLeadingMarging();
                BulletSpan bulletSpan = new BulletSpan(margin, paragraph.isEmpty(), paragraph.isFirst(), paragraph.isLast());
                mSpans2Process.addParagraphSpan(bulletSpan, paragraph, false);

                // if the paragraph has number spans, then remove it
                Effects.NUMBER.findSpans2Remove(str, paragraph, mSpans2Process);
            }
        }

        // add or remove spans
        mSpans2Process.process(str);
    }

    void findSpans2Remove(Spannable str, Paragraph paragraph, ParagraphSpanProcessor<Boolean> spans2Remove) {
        List<RTSpan<Boolean>> spans = getSpans(str, paragraph, SpanCollectMode.SPAN_FLAGS);
        for (RTSpan<Boolean> span : spans) {
            spans2Remove.addParagraphSpan(span, paragraph, true);
        }
    }

}
