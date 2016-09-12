package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MazeFragment extends Fragment {

    @BindView(R.id.maze)
    ImageView mMazeView;

    MazeCanvas mMazeCanvas;

    public MazeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_maze, container, false);

        ButterKnife.bind(this, rootView);

        mMazeCanvas= new MazeCanvas();

        return rootView;
    }

    @SuppressWarnings("ConstantConditions") //this fragment DOES have a layout
    public void draw(int direction){
        if(mMazeCanvas.getmBitmap() == null){
            mMazeCanvas.setBitmap(Bitmap.createBitmap(mMazeView.getWidth()/*100*/, mMazeView.getHeight()/*100*/, Bitmap.Config.ARGB_8888));
        }
        mMazeView.setImageBitmap(mMazeCanvas.addStep(direction));
    }
}
