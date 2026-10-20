package com.ldh.quiz.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ldh.quiz.QuizData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Man hinh tro choi dien cum tu khoa cho mot cau tu luan. */
@OnlyIn(Dist.CLIENT)
public class QuizScreen extends Screen {
    private static final Pattern BLANK = Pattern.compile("\\[\\[(.+?)\\]\\]");
    private static final int LH = 11;

    private enum Mode { PLAY, WIN, LOSE }

    private static final class Seg {
        final String text;
        final boolean blank;
        final boolean head;
        Seg(String text, boolean blank, boolean head) {
            this.text = text;
            this.blank = blank;
            this.head = head;
        }
    }

    private record Step(int p, int j, String ans) {}

    private final QuizData.Q q;
    private final List<List<Seg>> passages = new ArrayList<>();
    private final List<Step> steps = new ArrayList<>();
    private final List<String> pool = new ArrayList<>();
    private final Random rnd = new Random();

    private Mode mode = Mode.PLAY;
    private int k = 0;
    private int hearts = 3;
    private int xp = 0;
    private int pending = 0;
    private int correctOpt = -1;
    private List<String> options = new ArrayList<>();
    private final Set<Integer> wrong = new HashSet<>();

    // bo cuc
    private float scale = 1f;
    private int panelW = 400;
    private int x0 = 0;
    private final int pY = 30;
    private List<FormattedCharSequence> pLines = new ArrayList<>();
    private final List<List<FormattedCharSequence>> oLines = new ArrayList<>();
    private final List<int[]> oRect = new ArrayList<>();
    private List<FormattedCharSequence> endLines = new ArrayList<>();
    private int scrollY = 0;
    private int scrollMax = 0;

    public QuizScreen(int index) {
        super(Component.literal("Ôn thi"));
        List<QuizData.Q> all = QuizData.all();
        this.q = all.get(Math.max(0, Math.min(index - 1, all.size() - 1)));
        Set<String> uniq = new LinkedHashSet<>();
        for (int p = 0; p < q.s.size(); p++) {
            List<Seg> segs = parse(q.s.get(p));
            passages.add(segs);
            int j = 0;
            for (Seg s : segs) {
                if (s.blank) {
                    steps.add(new Step(p, j++, s.text));
                    uniq.add(s.text);
                }
            }
        }
        pool.addAll(uniq);
        setupStep();
    }

    private static List<Seg> parse(String s) {
        List<Seg> segs = new ArrayList<>();
        boolean first = true;
        for (String line : s.split("\n", -1)) {
            boolean head = line.startsWith("## ");
            if (head) {
                line = line.substring(3);
            }
            if (!first) {
                segs.add(new Seg("\n", false, false));
            }
            first = false;
            Matcher m = BLANK.matcher(line);
            int last = 0;
            while (m.find()) {
                if (m.start() > last) {
                    segs.add(new Seg(line.substring(last, m.start()), false, head));
                }
                segs.add(new Seg(m.group(1), true, false));
                last = m.end();
            }
            if (last < line.length()) {
                segs.add(new Seg(line.substring(last), false, head));
            }
        }
        return segs;
    }

    private MutableComponent build(int p, int curJ, boolean all) {
        MutableComponent out = Component.empty();
        int bi = 0;
        for (Seg s : passages.get(p)) {
            if (!s.blank) {
                MutableComponent c = Component.literal(s.text);
                if (s.head) {
                    c.withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
                }
                out.append(c);
            } else {
                MutableComponent c;
                if (all || bi < curJ) {
                    c = Component.literal(s.text).withStyle(ChatFormatting.GREEN);
                } else if (bi == curJ) {
                    c = Component.literal("[ ??? ]").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
                } else {
                    c = Component.literal("[ ... ]").withStyle(ChatFormatting.GRAY);
                }
                out.append(c);
                bi++;
            }
        }
        return out;
    }

    private void setupStep() {
        if (steps.isEmpty()) {
            return;
        }
        final Step st = steps.get(k);
        List<String> cands = new ArrayList<>(pool);
        cands.remove(st.ans());
        Collections.shuffle(cands, rnd);
        cands.sort(Comparator.comparingInt(x -> Math.abs(x.length() - st.ans().length())));
        List<String> top = new ArrayList<>(cands.subList(0, Math.min(6, cands.size())));
        Collections.shuffle(top, rnd);
        options = new ArrayList<>(top.subList(0, Math.min(3, top.size())));
        options.add(st.ans());
        Collections.shuffle(options, rnd);
        wrong.clear();
        correctOpt = -1;
        pending = 0;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        panelW = Math.min(440, this.width - 16);
        scale = 1f;
        if (mode == Mode.PLAY && !steps.isEmpty()) {
            Step st = steps.get(k);
            int curJ = pending > 0 ? st.j() + 1 : st.j();
            pLines = this.font.split(build(st.p(), curJ, false), panelW - 12);
            oLines.clear();
            oRect.clear();
            int y = pY + pLines.size() * LH + 10;
            for (String o : options) {
                List<FormattedCharSequence> ls = this.font.split(Component.literal(o), panelW - 16);
                oLines.add(ls);
                int h = ls.size() * LH + 6;
                oRect.add(new int[] {0, y, panelW, h});
                y += h + 4;
            }
            int total = y + 4;
            if (total > this.height) {
                scale = Math.max(0.4f, (float) this.height / total);
            }
        } else if (mode == Mode.WIN) {
            List<FormattedCharSequence> el = new ArrayList<>();
            el.addAll(this.font.split(Component.literal("Mẹo nhớ: " + q.m).withStyle(ChatFormatting.GOLD), panelW - 12));
            el.add(FormattedCharSequence.EMPTY);
            for (int p = 0; p < passages.size(); p++) {
                el.addAll(this.font.split(build(p, 0, true), panelW - 12));
                el.add(FormattedCharSequence.EMPTY);
            }
            endLines = el;
            int visible = (this.height - 30) - 34;
            scrollMax = Math.max(0, el.size() * LH - visible);
            scrollY = Math.max(0, Math.min(scrollY, scrollMax));
        }
        x0 = (int) ((this.width / scale - panelW) / 2);
        for (int[] r : oRect) {
            r[0] = x0;
        }
    }

    private void restart() {
        mode = Mode.PLAY;
        k = 0;
        hearts = 3;
        xp = 0;
        scrollY = 0;
        setupStep();
        rebuild();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (pending > 0) {
            pending--;
            if (pending == 0) {
                k++;
                if (k >= steps.size()) {
                    mode = Mode.WIN;
                } else {
                    setupStep();
                }
                rebuild();
            }
        }
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + h;
        g.fill(x, y, x + w, y + h, hov ? 0xFF4A56A0 : 0xFF2C3366);
        g.drawString(this.font, label, x + (w - this.font.width(label)) / 2, y + (h - 8) / 2, 0xFFFFFF);
    }

    private int btnY() {
        return this.height - 24;
    }

    private int btnW() {
        return (panelW - 8) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        if (mode == Mode.PLAY) {
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.scale(scale, scale, 1f);
            float vx = mx / scale;
            float vy = my / scale;
            g.fill(x0 - 6, 0, x0 + panelW + 6, (int) Math.ceil(this.height / scale), 0xD0101018);
            g.drawString(this.font, q.t, x0, 8, 0xFFAA00);
            String hud = "\u2665".repeat(Math.max(hearts, 0)) + "  " + xp + " XP";
            g.drawString(this.font, hud, x0 + panelW - this.font.width(hud), 8, 0xFF5555);
            g.fill(x0, 19, x0 + panelW, 22, 0xFF333344);
            g.fill(x0, 19, x0 + panelW * k / Math.max(1, steps.size()), 22, 0xFF5C7CFF);
            for (int i = 0; i < pLines.size(); i++) {
                g.drawString(this.font, pLines.get(i), x0 + 2, pY + i * LH, 0xFFFFFF);
            }
            for (int i = 0; i < options.size(); i++) {
                int[] r = oRect.get(i);
                boolean hov = vx >= r[0] && vx < r[0] + r[2] && vy >= r[1] && vy < r[1] + r[3];
                int col;
                if (wrong.contains(i)) {
                    col = 0xE0802020;
                } else if (i == correctOpt) {
                    col = 0xE0208040;
                } else if (hov) {
                    col = 0xE0383870;
                } else {
                    col = 0xE0202038;
                }
                g.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], col);
                g.fill(r[0], r[1], r[0] + r[2], r[1] + 1, 0xFF6670A0);
                g.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], 0xFF6670A0);
                g.fill(r[0], r[1], r[0] + 1, r[1] + r[3], 0xFF6670A0);
                g.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], 0xFF6670A0);
                List<FormattedCharSequence> ls = oLines.get(i);
                for (int j = 0; j < ls.size(); j++) {
                    g.drawString(this.font, ls.get(j), r[0] + 8, r[1] + 4 + j * LH, 0xFFFFFF);
                }
            }
            ps.popPose();
        } else {
            g.fill(x0 - 6, 0, x0 + panelW + 6, this.height, 0xD0101018);
            if (mode == Mode.WIN) {
                g.drawString(this.font, "Hoàn thành!  +" + xp + " XP  (" + q.t + ")", x0, 10, 0x55FF55);
                int top = 34;
                int bottom = this.height - 30;
                g.enableScissor(x0 - 6, top, x0 + panelW + 6, bottom);
                for (int i = 0; i < endLines.size(); i++) {
                    int y = top - scrollY + i * LH;
                    if (y > bottom || y < top - LH) {
                        continue;
                    }
                    g.drawString(this.font, endLines.get(i), x0 + 2, y, 0xFFFFFF);
                }
                g.disableScissor();
                g.drawString(this.font, "Cuộn chuột để đọc lại toàn văn", x0, bottom + 2, 0x888888);
            } else {
                g.drawString(this.font, "Hết tim!", x0, 10, 0xFF5555);
                g.drawString(this.font, "Đọc lại bài rồi thử lại nhé.", x0, 30, 0xFFFFFF);
            }
            button(g, x0, btnY(), btnW(), 18, "Chơi lại", mx, my);
            button(g, x0 + btnW() + 8, btnY(), btnW(), 18, "Đóng", mx, my);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) {
            return super.mouseClicked(mx, my, btn);
        }
        if (mode == Mode.PLAY) {
            if (pending > 0) {
                return true;
            }
            float vx = (float) (mx / scale);
            float vy = (float) (my / scale);
            for (int i = 0; i < oRect.size(); i++) {
                int[] r = oRect.get(i);
                if (vx >= r[0] && vx < r[0] + r[2] && vy >= r[1] && vy < r[1] + r[3]) {
                    if (wrong.contains(i)) {
                        return true;
                    }
                    if (options.get(i).equals(steps.get(k).ans())) {
                        correctOpt = i;
                        xp += 10;
                        pending = 10;
                        rebuild();
                    } else {
                        wrong.add(i);
                        hearts--;
                        if (hearts <= 0) {
                            mode = Mode.LOSE;
                            rebuild();
                        }
                    }
                    return true;
                }
            }
            return true;
        }
        int bx = (int) mx;
        int by = (int) my;
        if (by >= btnY() && by < btnY() + 18) {
            if (bx >= x0 && bx < x0 + btnW()) {
                restart();
            } else if (bx >= x0 + btnW() + 8 && bx < x0 + 2 * btnW() + 8) {
                this.onClose();
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (mode == Mode.WIN) {
            scrollY = Math.max(0, Math.min(scrollMax, scrollY - (int) (delta * 22)));
            return true;
        }
        return false;
    }
}
