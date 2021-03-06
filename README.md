
★画面上部のステータスバーの中央に日付と時刻を表示します。

![Screenshot_20210327-134148](https://user-images.githubusercontent.com/81674805/113226008-14814f80-92ca-11eb-9443-a36943e868f6.png)

車載用Androidヘッドユニット(日本では俗に中華ナビと呼ばれているようですが)は、一般的に横画面での利用なのでステータスバーの中央付近は空いています。そこでステータスバーの高さに合わせた時計を表示するアプリを作りました。

一般的なAndroid端末では、端末の起動後にBOOT_COMPLETEDがBroadcastされますが、車載用のヘッドユニットでは飛んでこないようです。またスリープ後にサービスが停止されてしまうようです。どうもヘッドユニットによってこのあたりの仕様は異なるようです。  
そこでこのアプリではユーザー補助サービスが開始したことも起動のトリガとして使えるようにしました。 

ただし、スリープ時にユーザー補助サービスが強制的にオフされてしまうようなヘッドユニットではこのアプリ単独での自動起動はできません。(Taskerなどの別アプリの助けが必要です。)  

それ以外に、当初トライしたACTION_SCREEN_ON、ACTION_USER_PRESENT、ACTION_HEADSET_PLUGのインテントを受けたことで表示を開始するコードも残っていますがこれがどれほど役立っているかは不明です。  

![クリップボード01](https://user-images.githubusercontent.com/81674805/113226064-2a8f1000-92ca-11eb-8c5f-3ba2d7c7bf6c.png)


【使い方】  
・初回起動時にオーバーレイ描画の許可を聞いてきますので許可してください。許可しないとこのアプリは動作しません。  
・画面のスイッチをオンにすると時計表示が開始されます。  
・端末を再起動してしばらく待っても表示が再開されない場合は①のユーザー補助サービスをオンにしてください。  
・端末を再起動して①のユーザー補助サービスがオフになってしまっていたら再度オンにしてさらに②の電池の最適化をしない設定にしてみてください。  
・端末を再起動してこれでもまだ①のユーザー補助サービスがオフされてしまい、かつヘッドユニット側にスリープ時にタスクをキルさせない設定が存在しなければ、残念ながらそのヘッドユニットではこのアプリ単独では自動起動はできません。  

・Taskerなどの外部アプリを使って(もちらんそれらが端末の再起動後に自動で起動できることが前提ではありますが)  
パッケージ名:com.toshi.barclock  
クラス名:com.toshi.barclock.RestartActivity  
というActivityにインテントを送ることで初期画面を経ずに時計表示を開始させることができるようになっています。  

・OS側で元々ステータスバーに表示している時刻表示は、隠し機能のシステムUI調整ツールでオフできる可能性があります。呼び出しができるボタンを用意しておきましたがヘッドユニットによってはうまくいかないかもしれません。  

・OSのデフォルト設定では表示開始時に通知領域にアイコンが表示され、同時に通知音が鳴ります。通知のオンオフの設定を行う画面を呼び出すためのボタンも用意しておきました。  

【当方で動作をチェックしたヘッドユニット】  
・Ownice C960:①のユーザー補助をオン、かつヘッドユニット側の拡張設定で本アプリをスリープ時に切らない設定にすると問題なく作動しました。  
・PUMPKIN AE0273B-06A:①②の設定を行ってもユーザー補助サービスがスリープ時にオフされてしまいました。Taskerで画面の状態オンをトリガーにしてインテントを送ると作動しました。  
・ヘッドユニット以外の一般的なAndroid端末でも動作はしますが、本来の時刻表示があるのでこのアプリの意味はあまりないかと思います。  
