package vn.tiki.sample.collectionview;

import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import vn.tale.viewholdersdemo.viewholder.TextModel;
import vn.tiki.collectionview.DataProvider;
import vn.tiki.collectionview.ListData;
import vn.tiki.sample.entity.Paging;

public class TodoDataProvider implements DataProvider<TextModel> {

  private static final int PER_PAGE = 5;
  private static final int LAST_PAGE = 10;

  @Override
  public Single<? extends ListData<TextModel>> fetch(int page) {
    return Single.fromCallable(() -> generateItems(page));
  }

  @Override
  public Single<? extends ListData<TextModel>> fetchNewest() {
    return Single.fromCallable(() -> generateItems(1));
  }

  private ListData<TextModel> generateItems(int page) throws Exception {
    Thread.sleep(1000);
    if (System.currentTimeMillis() % 2 == 0) {
      throw new Exception("Error");
    }
    final int startIndex = (page - 1) * PER_PAGE;
    final List<TextModel> result = new ArrayList<>(PER_PAGE);
    for (int i = 0; i < PER_PAGE; i++) {
      final int index = i + startIndex;
      result.add(new TextModel("Item " + index));
    }
    final Paging paging = Paging.builder()
        .currentPage(page)
        .lastPage(LAST_PAGE)
        .total(LAST_PAGE * PER_PAGE)
        .make();
    return new ListData<TextModel>() {
      @Override
      public List<TextModel> items() {
        return result;
      }

      @Override
      public vn.tiki.collectionview.Paging paging() {
        return paging;
      }
    };
  }
}
