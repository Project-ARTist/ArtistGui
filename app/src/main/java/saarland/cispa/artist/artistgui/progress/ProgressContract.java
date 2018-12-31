package saarland.cispa.artist.artistgui.progress;

import androidx.annotation.NonNull;

import saarland.cispa.artist.artistgui.base.BasePresenter;
import saarland.cispa.artist.artistgui.base.BaseView;

interface ProgressContract {
    interface View extends BaseView<ProgressContract.Presenter> {
        void onProgressStageChanged(int progress, @NonNull String packageName,
                                    @NonNull String stage);

        void onProgressDetailChanged(@NonNull String packageName, @NonNull String message);

        void onFinishedInstrumentation();
    }

    interface Presenter extends BasePresenter {
        void cancelInstrumentation();

        void destroy();
    }
}
